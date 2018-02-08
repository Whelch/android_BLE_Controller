package com.whelch.ledcontroller;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	private static final int LOCATION_PERMISION_CODE = 1;
	
	private static final String BED_DEVICE_NAME = "Bed Controller";

	private BluetoothAdapter btAdapter;
	private BluetoothScanCallback scanCallback;
	private BluetoothLeScanner btLeScanner;
	private Handler handler = new Handler();
	private BluetoothDevice bedController;

	private static final long SCAN_DURATION = 5000;

	@Override
	protected void onResume() {
		super.onResume();
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		BluetoothManager btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		btAdapter = btManager.getAdapter();
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			requestLocationPermission();
		} else {
			startScan();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch(requestCode) {
			case LOCATION_PERMISION_CODE:
				if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Log.d("PERMISSION_RESULT", "Fine location permission granted");
					startScan();
				}
		}
	}

	private void startScan() {
		if(!hasPermissions()) {
			return;
		}

		List<ScanFilter> filters = new ArrayList<>();
		ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();

		scanCallback = new BluetoothScanCallback();
		btLeScanner = btAdapter.getBluetoothLeScanner();
		btLeScanner.startScan(filters, settings, scanCallback);

		handler.postDelayed(this::stopScan, SCAN_DURATION);
	}

	private void stopScan() {
		btLeScanner.stopScan(scanCallback);
		connectToController();
	}
	
	private void connectToController() {
		if(bedController != null) {
			bedController.connectGatt(this, true, new BedGattCallback());
		}
	}

	private boolean hasPermissions() {
		if (!btAdapter.isEnabled()) {
			requestBluetoothEnable();
			return false;
		}
		return true;
	}
	
	@TargetApi(24)
	private void requestLocationPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("This app needs location access");
			builder.setMessage("Please grant location access so this app can scan for bluetooth devices.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(dialog ->
					requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISION_CODE)
			);
			builder.show();
		} else {
			startScan();
		}
	}

	private void requestBluetoothEnable() {
		Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBluetoothIntent, 2);
	}
	
	private class BedGattCallback extends BluetoothGattCallback {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			Log.d("GATT_CALLBACK", ""+status);
			gatt.discoverServices();
		}
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.d("GATT_CALLBACK", ""+status);
		}
	}

	private class BluetoothScanCallback extends ScanCallback {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			String deviceName = result.getScanRecord().getDeviceName();
			if (deviceName != null && deviceName.equals(BED_DEVICE_NAME)) {
				setBedController(result);
			}
		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			for(ScanResult result: results) {
				String deviceName = result.getScanRecord().getDeviceName();
				if (deviceName != null && deviceName.equals(BED_DEVICE_NAME)) {
					setBedController(result);
				}
			}
		}

		@Override public void onScanFailed(int errorCode) {
			Log.e("BLE_SCAN", "BLE Scan failed with code" + errorCode);
		}

		private void setBedController(ScanResult result) {
			bedController = result.getDevice();
		}
	}
}