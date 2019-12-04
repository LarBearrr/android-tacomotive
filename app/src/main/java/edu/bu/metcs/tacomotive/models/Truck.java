package edu.bu.metcs.tacomotive.models;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Truck {

    // Firebase Database Reference
    private DatabaseReference mDatabase;

    private static String path = "trucks";

    public String name;
    public String phone;
    public String address;
    public LatLng coordinates;
    public String userId;

    public Truck() {
        // Default constructor required for calls to DataSnapshot.getValue(Truck.class)
    }

    public Truck(String name, String phone, String address, LatLng coordinates, String userId) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.coordinates = coordinates;
        this.userId = userId;
    }

    public void save() {
        mDatabase = FirebaseDatabase.getInstance().getReference(path);

        String key = mDatabase.push().getKey();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, this);

        mDatabase.updateChildren(childUpdates);

        setPin(key, coordinates);
    }

    public void update() {

    }


    private void setPin(String key, LatLng coordinates) {

        mDatabase = FirebaseDatabase.getInstance().getReference("pins");
        GeoFire geoFire = new GeoFire(mDatabase);

        geoFire.setLocation(key, new GeoLocation(coordinates.latitude, coordinates.longitude));
    }

}