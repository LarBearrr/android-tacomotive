package edu.bu.metcs.tacomotive;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

import edu.bu.metcs.tacomotive.app.TacomotiveApplication;
import edu.bu.metcs.tacomotive.models.Truck;

public class AddTruckActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    EditText truckName, truckPhone;
    String address;
    LatLng coordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_truck);

        truckName = findViewById(R.id.truckNameEditText);
        truckPhone = findViewById(R.id.phoneEditTextId);

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
        final AutocompleteSupportFragment truckAddress = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.truckAddressAutocompleteFragment);
        // Specify the types of place data to return.
        truckAddress.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        truckAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("place", "Place: " + place.getAddress());
                coordinates = place.getLatLng();
                address = place.getAddress().toString();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("place", "An error occurred: " + status);
            }
        });
    }

    public void onAddTruck(View view) {
        Log.d("ADD", "Adding food truck...");
        String name = truckName.getText().toString();
        String phone = truckPhone.getText().toString();

        TacomotiveApplication app = (TacomotiveApplication) getApplication();

        FirebaseUser user = app.getUser();

        String userId = user.getUid();

        // Create new truck instance
        Truck truck = new Truck(name, phone, address, coordinates, userId);

        // Save truck to database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("trucks").child(name).setValue(truck);

        finish();
    }
}
