package com.example.myproject;

import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.example.myproject.ui.login.FavoritePlace;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity {

    private Button btn;
    private Button btn2;
    private Button btn3;
    private Button btn4;

    private EditText et;
    private EditText et2;
    private EditText et3;
    private FavoritePlace fp;

    private FirebaseDatabase database;
    DatabaseReference ref;
    private String name;
    private boolean exists;
    private boolean exists1;
    private String lat;
    private String lon;
    private Double platitude;
    private Double plongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        btn = (Button) findViewById(R.id.button);
        btn2 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);
        btn4 = (Button) findViewById(R.id.button4);
        et = (EditText) findViewById(R.id.editText);
        et2 = (EditText) findViewById(R.id.editText2);
        et3 = (EditText) findViewById(R.id.editText3);
        fp = new FavoritePlace();

        database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Favorite Places");

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                launchActivity();
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                launchActivityOne();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name = et.getText().toString();
                lat = et2.getText().toString();
                lon = et3.getText().toString();
                exists = false;
                 try{
                     //double dname = Double.parseDouble(name);
                     double dlat = Double.parseDouble(lat);
                     double dlon = Double.parseDouble(lon);

                     if(dlat> 90 || dlat < -90){

                         Toast.makeText(Main2Activity.this, "Incorrect Latitude", Toast.LENGTH_SHORT).show();
                     }

                     else if(dlon> 180 || dlat < -180){

                         Toast.makeText(Main2Activity.this, "Incorrect Longitude", Toast.LENGTH_SHORT).show();
                     }
                     else if( name.isEmpty()){

                         Toast.makeText(Main2Activity.this, "Missing name", Toast.LENGTH_SHORT).show();
                     }
                     else {


                         ref.addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                 for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                     String name2 = (String) snapshot.child("name").getValue();
                                     Log.v("TAG", "Name is: " + name2);

                                     if (name2.equals(name)) {
                                         exists = true;
                                         //Toast.makeText(Main2Activity.this, "Place already exists, use a different name", Toast.LENGTH_SHORT).show();
                                     }

                                 }
                                 if(exists == false) {


                                     fp.setName(name);
                                     fp.setLatitude(lat);
                                     fp.setLongitude(lon);


                                     ref.push().setValue(fp);

                                     //Toast.makeText(Main2Activity.this, et.getText(), Toast.LENGTH_SHORT).show();
                                     Toast.makeText(Main2Activity.this, "Inserted: "+ name, Toast.LENGTH_SHORT).show();
                                 }


                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError databaseError) {

                             }
                         });


                        }



                 }
                 catch ( Exception e){

                     Toast.makeText(Main2Activity.this, "Incorrect Inputs", Toast.LENGTH_SHORT).show();

                 }
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Use the LocationManager class to obtain GPS locations.
                LocationManager locationManager =
                        (LocationManager) getSystemService( LOCATION_SERVICE );
                Criteria criteria   = new Criteria( );
                String bestProvider = locationManager.getBestProvider( criteria, true );
                Location location   = locationManager.getLastKnownLocation( bestProvider );

                exists1 = false;

                platitude     = location.getLatitude( );
                plongitude    = location.getLongitude( );

                name = et.getText().toString();
                 if (name.isEmpty()){

                     Toast.makeText(Main2Activity.this, "Missing name", Toast.LENGTH_SHORT).show();


                 }else {


                     ref.addValueEventListener(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                             for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                 String name2 = (String) snapshot.child("name").getValue();
                                 Log.v("TAG", "Name is: " + name2);

                                 if (name2.equals(name)) {
                                     exists1 = true;
                                     //Toast.makeText(Main2Activity.this, "Place already exists, use a different name", Toast.LENGTH_SHORT).show();
                                 }

                             }
                             if(exists1 == false) {


                                 lat = Double.toString(platitude);
                                 lon = Double.toString(plongitude);

                                 fp = new FavoritePlace();
                                 fp.setName(name);
                                 fp.setLatitude(lat);
                                 fp.setLongitude(lon);

                                 ref.push().setValue(fp);
                                 Toast.makeText(Main2Activity.this, "Inserted: "+ name, Toast.LENGTH_SHORT).show();
                                // Toast.makeText(Main2Activity.this, "Current Location", Toast.LENGTH_SHORT).show();
                             }

                         }

                         @Override
                         public void onCancelled(@NonNull DatabaseError databaseError) {

                         }
                     });












                 }

            }
        });
    }

    private void launchActivity() {

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void launchActivityOne() {

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
