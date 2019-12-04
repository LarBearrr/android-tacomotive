package edu.bu.metcs.tacomotive;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String apiKey = getString(R.string.api_key);

        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        final PlacesClient placesClient = Places.createClient(this);


        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("place", "Place: " + place);
                onMapUpdate(place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("place", "An error occurred: " + status);
            }
        });
    }

    public void onMapUpdate(LatLng latLng) {
        LatLng coordinates = latLng;

        mMap.animateCamera(CameraUpdateFactory.newLatLng(coordinates));
        mMap.setMinZoomPreference(12);

        onSearchMap(coordinates);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng center = new LatLng(37.6922, -97.3376);

        mMap.animateCamera(CameraUpdateFactory.newLatLng(center));
        mMap.setMinZoomPreference(12);

        // Set a listener for info window events.
        mMap.setOnInfoWindowClickListener(this);

        onSearchMap(center);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Truck added to favorites",
                Toast.LENGTH_SHORT).show();


    }

    public void onSearchMap(final LatLng latLng) {

        Double lat = latLng.latitude;
        Double lng = latLng.longitude;

        // Source: https://github.com/firebase/geofire-android
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("pins");
        GeoFire geoFire = new GeoFire(mDatabase);

        // creates a new query around the users location with a radius of 1 km
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(lat, lng), 10);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));

                // Create a new references to the trucks database
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("trucks");

                // Retrieve an individual truck
                // Source: https://stackoverflow.com/a/30564863
                mDatabase.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        // Get values for the truck
                        String name = snapshot.child("name").getValue().toString();
                        String address = snapshot.child("address").getValue().toString();
                        String phone = snapshot.child("phone").getValue().toString();

                        // Parse the trucks coordinates
                        String latitude = snapshot.child("coordinates").child("latitude").getValue().toString();
                        String longitude = snapshot.child("coordinates").child("longitude").getValue().toString();

                        Double lat = Double.valueOf(latitude);
                        Double lng = Double.valueOf(longitude);

                        // Add a marker to the map
                        LatLng marker = new LatLng(lat, lng);
                        mMap.addMarker(new MarkerOptions().position(marker).title(name).snippet(address + "\r\n" + phone));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });


            }

            @Override
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });
    }

    public void onClickAddTruck(View view) {
        Intent intent = new Intent(this, AddTruckActivity.class);
        startActivity(intent);
    }
}
