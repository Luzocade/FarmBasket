package com.smartagriculture;

public class Addr {

    private String addressString;
    private double latitude,longitude;

    public Addr(){

    }

    public Addr(String address, double latitude, double longitude) {
        this.addressString = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddressString() {
        return addressString;
    }

    public void setAddressString(String addressString) {
        this.addressString = addressString;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
