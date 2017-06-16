package com.example.psycho.arduionoble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.content.Context;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.content.res.Resources.Theme;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "mine";
    Spinner spinner;
    SpinnerAdapter spinnerAdapter;
    Toolbar toolbar;


    private BluetoothAdapter bluetoothAdapter;
    private boolean scanning;
    private Handler handler;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        spinner = (Spinner) findViewById(R.id.spinner);
        spinnerAdapter = new SpinnerAdapter(toolbar.getContext(),new ArrayList<String>());
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                BluetoothDevice device = spinnerAdapter.getDevice(position);

                if (device !=null) {

                    if (scanning) {
                        bluetoothAdapter.stopLeScan(mLeScanCallback);
                        scanning = false;
                    }

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, DeviceFragment.newInstance(position, device))
                            .commit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        handler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (scanning) {
            menu.findItem(R.id.action_scan).setVisible(false);
            menu.findItem(R.id.action_scan_stop).setVisible(true);
            menu.findItem(R.id.action_progress_bar).setActionView(R.layout.progress_bar);
        } else {
            menu.findItem(R.id.action_scan).setVisible(true);
            menu.findItem(R.id.action_scan_stop).setVisible(false);
            menu.findItem(R.id.action_progress_bar).setActionView(null);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_scan:
                spinnerAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.action_scan_stop:
                scanLeDevice(false);
                break;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        spinnerAdapter = new SpinnerAdapter(toolbar.getContext(),new ArrayList<String>());
        spinner.setAdapter(spinnerAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        spinnerAdapter.clear();
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            scanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }

        invalidateOptionsMenu();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spinnerAdapter.addDevice(device);
                            spinnerAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    private class SpinnerAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {

        private final ThemedSpinnerAdapter.Helper dropDownHelper;

        List<BluetoothDevice> bluetoothDevices = new ArrayList<>();

        public SpinnerAdapter(Context context, List<String> objects) {
            super(context, R.layout.spinner_list_item, objects);
            dropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {

            View view;

            if (convertView == null) {
                LayoutInflater inflater = dropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(R.layout.double_spinner_list_item, parent, false);
            } else {
                view = convertView;
            }

            TextView nameView = (TextView) view.findViewById(R.id.text_name);
            nameView.setText(bluetoothDevices.get(position).getName());

            TextView addressView = (TextView) view.findViewById(R.id.text_address);
            addressView.setText(bluetoothDevices.get(position).getAddress());

            return view;
        }

        @Override
        public Theme getDropDownViewTheme() {
            return dropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Theme theme) {
            dropDownHelper.setDropDownViewTheme(theme);
        }

        public void addDevice(BluetoothDevice device) {
            if (!bluetoothDevices.contains(device)) {
                bluetoothDevices.add(device);
                add(device.getName());
                Log.d(TAG, "Add device: "+device.getName());
            }
        }

        public BluetoothDevice getDevice(int position){
            return bluetoothDevices.get(position);
        }

        public void clear() {
            super.clear();
            bluetoothDevices.clear();
            notifyDataSetChanged();
        }
    }

}