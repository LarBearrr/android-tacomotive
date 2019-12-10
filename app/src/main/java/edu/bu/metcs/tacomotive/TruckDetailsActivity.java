package edu.bu.metcs.tacomotive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import edu.bu.metcs.tacomotive.app.TacomotiveApplication;

public class TruckDetailsActivity extends AppCompatActivity {


    FirebaseUser user;

    String truckId;
    TextView truckName, truckAddress, truckPhone;
    Switch isFavoriteSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_truck_details);

        TacomotiveApplication app = (TacomotiveApplication) getApplication();
        // Set the current user id
        user = app.getUser();


        truckId = getIntent().getStringExtra("truckId");

        truckName = findViewById(R.id.truckNameTextView);
        truckAddress = findViewById(R.id.truckAddressTextView);
        truckPhone = findViewById(R.id.truckPhoneTextView);

        isFavoriteSwitch = (Switch) findViewById(R.id.isFavoriteSwitch);

        setTruck(truckId);

        // Set a checked change listener for switch button
        isFavoriteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setFavorite();
                }
                else {
                    unsetFavorite();
                }
            }
        });
    }

    private void setTruck(String truckId) {
        // Create a new references to the trucks database
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        // Retrieve an individual truck
        // Source: https://stackoverflow.com/a/30564863
        mDatabase.child("trucks").child(truckId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                // Get values for the truck
                String name = snapshot.child("name").getValue().toString();
                String address = snapshot.child("address").getValue().toString();
                String phone = snapshot.child("phone").getValue().toString();

                truckName.setText(name);
                truckAddress.setText(address);
                truckPhone.setText(phone);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mDatabase.child("user-favorites").child(user.getUid()).child(truckId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Boolean isFavorite = Boolean.parseBoolean(snapshot.getValue().toString());
                    isFavoriteSwitch.setChecked(isFavorite);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void setFavorite() {
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Create new favorite at /user-favorites/$userid/$truckId
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/user-favorites/" + user.getUid() + "/" + truckId, true);

        mDatabase.updateChildren(childUpdates);

        // Show the switch button checked status as toast message
        Toast.makeText(TruckDetailsActivity.this, "Truck added to favorites!", Toast.LENGTH_SHORT).show();
    }

    private void unsetFavorite()
    {
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference("user-favorites");

        mDatabase.child(user.getUid()).child(truckId).removeValue();

        // Show the switch button checked status as toast message
        Toast.makeText(TruckDetailsActivity.this,
                "Truck removed from favorites!", Toast.LENGTH_LONG).show();
    }
}
