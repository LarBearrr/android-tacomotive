package edu.bu.metcs.tacomotive;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.*;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.loopj.android.http.*;

import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

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

        onSearchMap(center);
    }

    public void onSearchMap(LatLng latLng) {

        String lat = String.valueOf(latLng.latitude);
        String lng = String.valueOf(latLng.longitude);

        /**
         * Async HTTP Request
         * Source: https://loopj.com/android-async-http/
         */
        HttpUtils.get("businesses/search?term=tacos&latitude=" + lat + "&longitude=" + lng, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                // If the response is JSONObject instead of expected JSONArray
                Log.d("trucks", "---------------- this is response : " + response);
                try {
                    String truckResponse = response.toString();

                    // Adapted from: https://stackoverflow.com/questions/22687771/how-to-convert-jsonobjects-to-jsonarray
                    JSONObject truckObject = new JSONObject(truckResponse);
                    JSONArray truckArray = truckObject.getJSONArray("businesses");

                    for(int i=0; i<truckArray.length(); i++) {
                        JSONObject truck = truckArray.getJSONObject(i);

                        JSONObject coordinates = truck.getJSONObject("coordinates");

                        String truckName = truck.getString("name");

                        Double truckLat = coordinates.getDouble("latitude");
                        Double truckLng = coordinates.getDouble("longitude");

                        Log.d("truck", "latitude: " + truckLat + ", longitude: " + truckLng);

                        // Add a marker
                        LatLng marker = new LatLng(truckLat, truckLng);
                        mMap.addMarker(new MarkerOptions().position(marker).title(truckName));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray trucks) {
                // Pull out the first truck from the response
                Log.d("trucks", "Successfull request made...");
            }
        });
    }

    public void onClickAddTruck(View view) {
        Intent intent = new Intent(this, AddTruckActivity.class);
        startActivity(intent);
    }
}
