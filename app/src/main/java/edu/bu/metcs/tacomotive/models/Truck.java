package edu.bu.metcs.tacomotive.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Truck {

    public String name;
    public String phone;
    public String address;
    public LatLng coordinates;
    public String userId;

    public Truck() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Truck(String name, String phone, String address, LatLng coordinates, String userId) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.coordinates = coordinates;
        this.userId = userId;
    }

}