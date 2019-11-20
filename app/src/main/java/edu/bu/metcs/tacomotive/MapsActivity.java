package edu.bu.metcs.tacomotive;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.*;
import com.loopj.android.http.*;

import java.net.HttpURLConnection;

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

        /**
         * Async HTTP Request
         * Source: https://loopj.com/android-async-http/
         */
        HttpUtils.get("businesses/search?term=tacos&latitude=37.6922&longitude=-97.3376", null, new JsonHttpResponseHandler() {
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

                        Double lat = coordinates.getDouble("latitude");
                        Double lng = coordinates.getDouble("longitude");

                        Log.d("truck", "latitude: " + lat + ", longitude: " + lng);

                        // Add a marker in Sydney and move the camera
                        LatLng marker = new LatLng(lat, lng);
                        mMap.addMarker(new MarkerOptions().position(marker).title(truckName));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray trucks) {
                // Pull out the first event on the public timeline
                Log.d("trucks", "Successfull request made...");
            }
        });
    }
}
