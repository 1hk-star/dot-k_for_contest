package com.example.my_sensor;

public class Beacon {
    private String address;
    private int rssi;
    private int txPower;
    private String now;
    private String name;

    public Beacon(String address, int rssi, String now, String name, int txPower){
        this.address=address;
        this.rssi=rssi;
        this.now=now;
        this.name=name;
        this.txPower=txPower;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getTxPower() {
        return txPower;
    }

    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    public String getNow() {
        return now;
    }

    public void setNow(String now) {
        this.now = now;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
