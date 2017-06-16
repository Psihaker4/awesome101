package com.example.psycho.arduionoble;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeviceFragment extends Fragment {

    private static final String TAG = "mine";

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_DEVICE_ADDRESS = "device_address";
    private static final String ARG_DEVICE_NAME = "device_name";

    private TextView connectionState;
    private Button connectButton;
    private ControlCircleLayout controlLayout;

    private boolean connected = false;
    private String deviceAddress;

    private BLeService bluetoothLeService;
    private BluetoothGattCharacteristic xGattCharacteristic;
    private BluetoothGattCharacteristic yGattCharacteristic;

    private List<BluetoothGattCharacteristic> queue = new ArrayList<>();
    private List<Integer> queueVals = new ArrayList<>();

    private int preX, preY;

    public DeviceFragment() {
    }

    public static DeviceFragment newInstance(int sectionNumber, BluetoothDevice bluetoothDevice) {
        DeviceFragment fragment = new DeviceFragment();
        Bundle args = new Bundle();

        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_DEVICE_NAME, bluetoothDevice.getName());
        args.putString(ARG_DEVICE_ADDRESS, bluetoothDevice.getAddress());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_device, container, false);
        TextView deviceName = (TextView) rootView.findViewById(R.id.device_name);
        deviceName.setText(getArguments().getString(ARG_DEVICE_NAME));

        final TextView deviceAddressView = (TextView) rootView.findViewById(R.id.device_address);
        deviceAddressView.setText(getArguments().getString(ARG_DEVICE_ADDRESS));

        deviceAddress = getArguments().getString(ARG_DEVICE_ADDRESS);

        controlLayout = (ControlCircleLayout) rootView.findViewById(R.id.controls);
        controlLayout.setupLayout();

        controlLayout.setOnCommandListener(new ControlCircleLayout.OnCommandListener() {
            @Override
            public void onChange(int x, int y) {

                if (x != preX || y != preY) {

                    if (x != preX) {
                        Log.d(TAG, "onChange: X");
                        //queue.add(xGattCharacteristic);
                        //queueVals.add(x);
                        writeChar(xGattCharacteristic,x);
                        preX = x;
                    }

                    if (y != preY) {
                        Log.d(TAG, "onChange: Y");
                        writeChar(yGattCharacteristic,y);
                        //queue.add(yGattCharacteristic);
                        //queueVals.add(y);
                        preY = y;
                    }

                    //writeChar(queue.get(0), queueVals.get(0));
                    //queue.remove(0);
                    //queueVals.remove(0);
                }
            }
        });

        connectionState = (TextView) rootView.findViewById(R.id.connection_state);
        connectButton = (Button) rootView.findViewById(R.id.connect_button);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                if (connected) {
                    bluetoothLeService.disconnect();
                } else {
                    bluetoothLeService.connect(deviceAddress);
                }
            }
        });



        Intent gattServiceIntent = new Intent(getContext(), BLeService.class);
        getActivity().bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "onCreateView: createBind"+deviceAddress);

        return rootView;
    }

    void writeChar(BluetoothGattCharacteristic chare, int val){
        if (chare != null) {
            final BluetoothGattCharacteristic characteristic = chare;
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                bluetoothLeService.writeCharacteristic(characteristic, val);
            }
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initializeService()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                getActivity().finish();
            }
            bluetoothLeService.connect(deviceAddress);
            Log.d(TAG, "onServiceConnected: connect");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (GattActions.CONNECTED.equals(action)) {
                Log.d(TAG, "onReceive: connect");
                connected = true;
                updateConnectionState("Connected");
            } else if (GattActions.DISCONNECTED.equals(action)) {
                Log.d(TAG, "onReceive: disconnect");
                connected = false;
                updateConnectionState("Disconnected");
            } else if (GattActions.SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "onReceive: serD");
                getGattCharacteristics(bluetoothLeService.getSupportedGattServices());
            } else if (GattActions.DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(GattActions.EXTRA_DATA));
                //Log.d(TAG, "onReceive: receive");
//                if(queue.size()!=0) {
//                    writeChar(queue.get(0), queueVals.get(0));
//                    queue.remove(0);
//                    queueVals.remove(0);
//                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getContext().registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bluetoothLeService != null ) {
            final boolean result = bluetoothLeService.connect(deviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unbindService(serviceConnection);
        bluetoothLeService = null;
    }

    private void updateConnectionState(final String text) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionState.setText(text);
                if (text.equals("Connected")) {
                    connectionState.setTextColor(Color.argb(255,0,255,0));
                    connectButton.setText("Disconnect");
                } else {
                    connectionState.setTextColor(Color.argb(255,255,0,0));
                    connectButton.setText("Connect");
                }

            }
        });
    }
    private void getGattCharacteristics(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                String uuid = gattCharacteristic.getUuid().toString();
                if(GattAttributes.lookup(uuid).equals("X")){
                    xGattCharacteristic = gattCharacteristic;
                } else if(GattAttributes.lookup(uuid).equals("Y")){
                    yGattCharacteristic = gattCharacteristic;
                }
            }
        }

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GattActions.CONNECTED);
        intentFilter.addAction(GattActions.DISCONNECTED);
        intentFilter.addAction(GattActions.SERVICES_DISCOVERED);
        intentFilter.addAction(GattActions.DATA_AVAILABLE);
        return intentFilter;
    }

}