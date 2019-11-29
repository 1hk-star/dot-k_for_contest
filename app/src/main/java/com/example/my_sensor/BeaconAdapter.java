package com.example.my_sensor;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.Vector;

public class BeaconAdapter extends BaseAdapter {
    private Vector<Beacon> beacons;
    private LayoutInflater layoutInflater;

    public BeaconAdapter(Vector<Beacon> beacons, LayoutInflater layoutInflater){
        this.beacons=beacons;
        this.layoutInflater=layoutInflater;
    }
    @Override
    public int getCount(){
        return beacons.size();
    }
    @Override
    public Object getItem(int position){
        return beacons.get(position);
    }
    @Override
    public long getItemId(int position){
        return 0;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        // not used
        return convertView;
    }
}