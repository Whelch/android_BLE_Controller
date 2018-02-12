package com.whelch.ledcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements BluetoothLeUart.Callback {

    // UI elements
    private EditText input;
    private Button   send;

    // Bluetooth LE UART instance.  This is defined in BluetoothLeUart.java.
    private BluetoothLeUart uart;

    // Write some text to the messages text view.
    // Care is taken to do this on the main UI thread so writeLine can be called from any thread
    // (like the BTLE callback).
    private void writeLine(final CharSequence text) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                messages.append(text);
//                messages.append("\n");
//            }
//        });
    }

    // Handler for mouse click on the send button.
    public void sendClick(View view) {
        StringBuilder stringBuilder = new StringBuilder();
        String message = input.getText().toString();

        // We can only send 20 bytes per packet, so break longer messages
        // up into 20 byte payloads
        int len = message.length();
        int pos = 0;
        while(len != 0) {
            stringBuilder.setLength(0);
            if (len>=20) {
                stringBuilder.append(message.toCharArray(), pos, 20 );
                len-=20;
                pos+=20;
            }
            else {
                stringBuilder.append(message.toCharArray(), pos, len);
                len = 0;
            }
            uart.send(stringBuilder.toString());
        }
        // Terminate with a newline character if requests
//        newline = (CheckBox) findViewById(R.id.newline);
//        if (newline.isChecked()) {
//            stringBuilder.setLength(0);
//            stringBuilder.append("\n");
//            uart.send(stringBuilder.toString());
//        }
    }
	
	/**
	 * Sends a command to the bluetooth device, if it's connected.
	 */
	public void sendCommand(String command) {
    	if(uart != null) {
    		uart.send(command);
		}
	}
    
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.controller_layout);
        
		// Initialize UART.
		uart = new BluetoothLeUart(getApplicationContext());

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new BedControllerPagerAdapter(getSupportFragmentManager()));
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        
        // Grab references to UI elements.
//        messages = (TextView) findViewById(R.id.messages);
//        input = (EditText) findViewById(R.id.input);


//        send = (Button)findViewById(R.id.send);
//        send.setClickable(false);
//        send.setEnabled(false);
    }

    // OnResume, called right before UI is displayed.  Connect to the bluetooth device.
    @Override
    protected void onResume() {
        super.onResume();
        writeLine("Scanning for devices ...");
        uart.registerCallback(this);
        uart.connectFirstAvailable();
    }

    // OnStop, called right before the activity loses foreground focus.  Close the BTLE connection.
    @Override
    protected void onStop() {
        super.onStop();
        uart.unregisterCallback(this);
        uart.disconnect();
    }

    // UART Callback event handlers.
    @Override
    public void onConnected(BluetoothLeUart uart) {
        // Called when UART device is connected and ready to send/receive data.
        writeLine("Connected!");
    }

    @Override
    public void onConnectFailed(BluetoothLeUart uart) {
        // Called when some error occured which prevented UART connection from completing.
        writeLine("Error connecting to device!");
    }

    @Override
    public void onDisconnected(BluetoothLeUart uart) {
        // Called when the UART device disconnected.
        writeLine("Disconnected!");
    }

    @Override
    public void onReceive(BluetoothLeUart uart, BluetoothGattCharacteristic rx) {
        // Called when data is received by the UART.
        writeLine("Received: " + rx.getStringValue(0));
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        // Called when a UART device is discovered (after calling startScan).
        writeLine("Found device : " + device.getAddress());
        writeLine("Waiting for a connection ...");
    }

    @Override
    public void onDeviceInfoAvailable() {
        writeLine(uart.getDeviceInfo());
    }
}
