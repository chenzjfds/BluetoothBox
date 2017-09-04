package com.actions.bluetoothbox.widget.fileselector;

import java.util.Comparator;
import java.util.HashMap;

public class FileCompare implements Comparator<HashMap<String,Object>>{
    @Override
    public int compare(HashMap<String, Object> lhs, HashMap<String, Object> rhs) {
        String n1= (String) lhs.get(FileSelector.NAME);
        String n2= (String) rhs.get(FileSelector.NAME);
        return n1.toUpperCase().compareTo(n2.toUpperCase());
    }
}
