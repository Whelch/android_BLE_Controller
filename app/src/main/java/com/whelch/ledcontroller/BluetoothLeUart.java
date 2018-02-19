package com.whelch.ledcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;

import com.whelch.ledcontroller.model.BreathingState;
import com.whelch.ledcontroller.model.PingPongState;
import com.whelch.ledcontroller.model.RainbowState;
import com.whelch.ledcontroller.model.StateType;
import com.whelch.ledcontroller.model.StripState;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BluetoothLeUart extends BluetoothGattCallback {
	
	// UUIDs for UART service and associated characteristics.
	public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
	public static UUID TX_UUID   = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
	public static UUID RX_UUID   = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
	
	// UUID for the UART BTLE client characteristic which is necessary for notifications.
	public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	
	// UUIDs for the Controller state and associated characteristics.
	public static UUID MONITOR_UUID					= UUID.fromString("857f0001-e2ab-4672-aa3f-82af1e889d27");
	public static UUID MONITOR_STRIP_STATE_UUID		= UUID.fromString("857f0002-e2ab-4672-aa3f-82af1e889d27");
	public static UUID MONITOR_RAINBOW_STATE_UUID	= UUID.fromString("857f0003-e2ab-4672-aa3f-82af1e889d27");
	public static UUID MONITOR_PINGPONG_STATE_UUID	= UUID.fromString("857f0004-e2ab-4672-aa3f-82af1e889d27");
	public static UUID MONITOR_BREATHING_STATE_UUID	= UUID.fromString("857f0005-e2ab-4672-aa3f-82af1e889d27");
	
	public static String BED_DEVICE_NAME = "Bed Controller";
	
	// Internal UART state.
	private Context context;
	private BluetoothAdapter adapter;
	private BluetoothLeScanner leScanner;
	private BluetoothGatt gatt;
	private BluetoothGattCharacteristic tx;
	
	private ScanCallback scanCallback;
	
	private MainActivity activity;
	
	private List<Runnable> commandQueue = new ArrayList<>();
	
	public BluetoothLeUart(Context context, MainActivity activity) {
		super();
		this.context = context;
		this.adapter = BluetoothAdapter.getDefaultAdapter();
		this.leScanner = adapter.getBluetoothLeScanner();
		this.gatt = null;
		this.tx = null;
		this.activity = activity;
	}
	
	public void send(byte[] data) {
		if (tx != null) {
			if(commandQueue.size() > 0) {
				commandQueue.add(() -> {
					tx.setValue(data);
					gatt.writeCharacteristic(tx);
				});
			} else {
				tx.setValue(data);
				gatt.writeCharacteristic(tx);
			}
		}
	}
	
	public void connectFirstAvailable() {
		disconnect();
		stopScan();
		startScan();
	}
	
	public void disconnect() {
		if (gatt != null) {
			gatt.close();
		}
		
		gatt = null;
		tx = null;
		activity.setDisconnected();
	}
	
	public void startScan() {
		if (leScanner != null) {
			scanCallback = new BluetoothScanCallback();
			leScanner.startScan(null, new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback);
			activity.setSearching();
		}
	}
	
	public void stopScan() {
		if (leScanner != null && scanCallback != null) {
			leScanner.stopScan(scanCallback);
			scanCallback = null;
		}
	}
	
	private void updateFromCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (characteristic.getUuid().equals(MONITOR_STRIP_STATE_UUID)) {
			byte val = characteristic.getValue()[0];
			StripState state = new StripState();
			state.leftPost = (val & (1)) > 0;
			state.leftRail = (val & (1 << 1)) > 0;
			state.rightPost = (val & (1 << 2)) > 0;
			state.rightRail = (val & (1 << 3)) > 0;
			activity.updateState(StateType.strip, state);
		} else if (characteristic.getUuid().equals(MONITOR_RAINBOW_STATE_UUID)) {
			byte[] val = characteristic.getValue();
			RainbowState state = new RainbowState();
			state.active = val[0] > 0;
			state.repeat = val[1];
			state.duration = val[2];
			activity.updateState(StateType.rainbow, state);
		} else if (characteristic.getUuid().equals(MONITOR_PINGPONG_STATE_UUID)) {
			byte[] val = characteristic.getValue();
			PingPongState state = new PingPongState();
			state.active = val[0] > 0;
			state.spread = val[1];
			state.duration = val[2];
			state.dark = val[3] > 0;
			state.easing = Easing.fromByte(val[4]);
			activity.updateState(StateType.pingPong, state);
		} else if (characteristic.getUuid().equals(MONITOR_BREATHING_STATE_UUID)) {
			byte[] val = characteristic.getValue();
			BreathingState state = new BreathingState();
			state.active = val[0] > 0;
			state.minIntensity = val[1];
			state.maxIntensity = val[2];
			state.duration = val[3];
			state.easing = Easing.fromByte(val[4]);
			activity.updateState(StateType.breathing, state);
		}
	}
	
	private void runNextCommand() {
		if(commandQueue.size() > 0) {
			Runnable command = commandQueue.remove(0);
			command.run();
		}
	}
	
	// Handlers for BluetoothGatt and LeScan events.
	@Override
	public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
		super.onConnectionStateChange(gatt, status, newState);
		if (newState == BluetoothGatt.STATE_CONNECTED) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				activity.setConnected();
				// Connected to device, start discovering services.
				if (!gatt.discoverServices()) {
					activity.setDisconnected();
				}
			}
			else {
				activity.setDisconnected();
			}
		}
		else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
			activity.setDisconnected();
		}
	}
	
	@Override
	public void onServicesDiscovered(BluetoothGatt gatt, int status) {
		super.onServicesDiscovered(gatt, status);
		
		// Save reference to each UART characteristic.
		tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
		
		// Register
		BluetoothGattService monitorService = gatt.getService(MONITOR_UUID);
		BluetoothGattCharacteristic stripStateChar = monitorService.getCharacteristic(MONITOR_STRIP_STATE_UUID);
		BluetoothGattCharacteristic rainbowStateChar = monitorService.getCharacteristic(MONITOR_RAINBOW_STATE_UUID);
		BluetoothGattCharacteristic pingPongStateChar = monitorService.getCharacteristic(MONITOR_PINGPONG_STATE_UUID);
		BluetoothGattCharacteristic breathingStateChar = monitorService.getCharacteristic(MONITOR_BREATHING_STATE_UUID);
		
		gatt.setCharacteristicNotification(stripStateChar, true);
		gatt.setCharacteristicNotification(rainbowStateChar, true);
		gatt.setCharacteristicNotification(pingPongStateChar, true);
		gatt.setCharacteristicNotification(breathingStateChar, true);
		
		// EnableNotificationChanges
		BluetoothGattDescriptor stripStateDesc = stripStateChar.getDescriptor(CLIENT_UUID);
		stripStateDesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		commandQueue.add(() -> gatt.writeDescriptor(stripStateDesc));
		
		BluetoothGattDescriptor rainbowStateDesc = stripStateChar.getDescriptor(CLIENT_UUID);
		rainbowStateDesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		commandQueue.add(() -> gatt.writeDescriptor(rainbowStateDesc));
		
		BluetoothGattDescriptor pingPongStateDesc = stripStateChar.getDescriptor(CLIENT_UUID);
		pingPongStateDesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		commandQueue.add(() -> gatt.writeDescriptor(pingPongStateDesc));
		
		BluetoothGattDescriptor breathingStateDesc = stripStateChar.getDescriptor(CLIENT_UUID);
		breathingStateDesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		commandQueue.add(() -> gatt.writeDescriptor(breathingStateDesc));
		
		// Queue initial state reads
		commandQueue.add(() -> gatt.readCharacteristic(breathingStateChar));
		commandQueue.add(() -> gatt.readCharacteristic(stripStateChar));
		commandQueue.add(() -> gatt.readCharacteristic(rainbowStateChar));
		commandQueue.add(() -> gatt.readCharacteristic(pingPongStateChar));
		
		runNextCommand();
	}
	
	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
		super.onCharacteristicChanged(gatt, characteristic);
		
		updateFromCharacteristic(characteristic);
	}
	
	@Override
	public void onCharacteristicRead (BluetoothGatt g, BluetoothGattCharacteristic characteristic, int status) {
		super.onCharacteristicRead(g, characteristic, status);
		
		updateFromCharacteristic(characteristic);
		runNextCommand();
	}
	
	@Override
	public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
		super.onCharacteristicWrite(gatt, characteristic, status);
		runNextCommand();
	}
	
	@Override
	public void onDescriptorWrite (BluetoothGatt g, BluetoothGattDescriptor characteristic, int status) {
		super.onDescriptorWrite(g, characteristic, status);
		runNextCommand();
	}
	
	
	/**
	 * BLE scan callback handler
	 */
	private class BluetoothScanCallback extends ScanCallback {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			String deviceName = result.getScanRecord().getDeviceName();
			if (deviceName != null && deviceName.equals(BED_DEVICE_NAME)) {
				connectToDevice(result);
			}
		}
		
		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			for(ScanResult result: results) {
				String deviceName = result.getScanRecord().getDeviceName();
				if (deviceName != null && deviceName.equals(BED_DEVICE_NAME)) {
					connectToDevice(result);
				}
			}
		}
		
		@Override
		public void onScanFailed(int errorCode) {
			Log.e("BLE_SCAN", "BLE Scan failed with code" + errorCode);
			activity.setDisconnected();
		}
		
		private void connectToDevice(ScanResult result) {
			if (gatt == null) {
				stopScan();
				gatt = result.getDevice().connectGatt(context, true, BluetoothLeUart.this);
			}
		}
	}
	
}
