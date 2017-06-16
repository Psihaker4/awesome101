package com.example.psycho.arduionoble;

import java.util.HashMap;

public class GattAttributes {

    private static HashMap<String, String> attributes = new HashMap();
    public static String ARDUINO_SERVICE = "0000aaaa-0000-1000-8000-00805f9b34fb";
    public static String ARDUINO_Y = "0000aaa0-0000-1000-8000-00805f9b34fb";
    public static String ARDUINO_X = "0000aaa1-0000-1000-8000-00805f9b34fb";

    static {
        attributes.put(ARDUINO_SERVICE, "Arduino Service");
        attributes.put(ARDUINO_X, "X");
        attributes.put(ARDUINO_Y, "Y");
    }

    public static String lookup(String uuid) {
        String name = attributes.get(uuid);
        return name == null ? "Unknown" : name;
    }

}
