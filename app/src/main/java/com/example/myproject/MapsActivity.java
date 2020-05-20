package com.example.myproject;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myproject.ui.login.FavoritePlace;
import com.example.myproject.ui.login.Main3Activity;
import com.example.myproject.ui.login.PlaceAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    private GoogleMap mMap;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private MarkerOptions mMarkerOptions;
    private MarkerOptions mMarkerOptionsDest;
    private MarkerOptions mMarkerOptionsDest2;
    private LatLng mOrigin;
    private LatLng mDestination;
    private LatLng mDestination2;
    private Polyline mPolyline;
    private ArrayList<PolyLine> PolyLines = new ArrayList<>();
    //private Marker Selected = null;
    private Button btn;
    private Button btn2;
    //new
    private GeoApiContext mGeoApiContext = null;
    ArrayList<String> Places = new ArrayList<>();

    PlaceAdapter adapter;

    //database
    FirebaseDatabase database;
    private String name;
    //private String KeyValue ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btn = (Button) findViewById(R.id.button_1);
        btn2 = (Button) findViewById(R.id.button_2);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                launchActivity();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                launchActivityTwo();
            }
        });


    }

    private void launchActivity() {

        Intent intent = new Intent(this, Main2Activity.class);
        startActivity(intent);
    }

    private void launchActivityTwo() {

        Intent intent = new Intent(this, Main3Activity.class);
        startActivity(intent);
    }

    private void addPolylines(final DirectionsResult res){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                double timeTaken = 99999998;
                if (PolyLines.size()>0){
                    for( PolyLine mpolyline: PolyLines){
                        mpolyline.getPolyline().remove();
                    }
                    PolyLines.clear();
                    PolyLines = new ArrayList<>();
                }
                for(DirectionsRoute r: res.routes){
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(r.overviewPolyline.getEncodedPath());
                    List<LatLng> newDecodedPath = new ArrayList<>();
                    for(com.google.maps.model.LatLng latLng: decodedPath){
                        newDecodedPath.add(new LatLng(latLng.lat, latLng.lng));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGrey));
                    polyline.setClickable(true);
                    PolyLines.add(new PolyLine(polyline, r.legs[0]));
                    double tmpTime = r.legs[0].duration.inSeconds;
                    if(tmpTime < timeTaken){
                        timeTaken = tmpTime;
                        onPolylineClick(polyline);
                    }
                }
            }
        });
    }


    private void calculateDirections(MarkerOptions marker, LatLng location){


        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(marker.getPosition().latitude, marker.getPosition().longitude
        );
        DirectionsApiRequest dir = new DirectionsApiRequest(mGeoApiContext);
        dir.alternatives(true);
        dir.origin(new com.google.maps.model.LatLng(location.latitude, location.longitude)
        );

        dir.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                addPolylines(result);
            }

            @Override
            public void onFailure(Throwable e) {


            }
        });
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



        RecyclerView rvPlaces = (RecyclerView) findViewById(R.id.my_recycler_view);



        // Create adapter passing in the sample user data
        adapter = new PlaceAdapter(Places);
        // Attach the adapter to the recyclerview to populate items
        rvPlaces.setAdapter(adapter);
        // Set layout manager to position the items
        rvPlaces.setLayoutManager(new LinearLayoutManager(this));







        mMap = googleMap;
        mMap.setOnPolylineClickListener(this);
        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_api_key)).build();

        }
        getMyLocation();

        // Show error dialog if GoolglePlayServices not available.
        if ( !isGooglePlayServicesAvailable( ) ) {
            finish( );
        }

        // Use the LocationManager class to obtain GPS locations.
        LocationManager locationManager =
                (LocationManager) getSystemService( LOCATION_SERVICE );
        Criteria criteria   = new Criteria( );
        String bestProvider = locationManager.getBestProvider( criteria, true );
        Location location   = locationManager.getLastKnownLocation( bestProvider );

        double latitude     = location.getLatitude( );
        double longitude    = location.getLongitude( );
        mOrigin       = new LatLng( latitude, longitude );

        mMarkerOptions = new MarkerOptions( ).position( mOrigin ).title( "Current Location" );
        mMap.addMarker(mMarkerOptions);

        mMap.animateCamera( CameraUpdateFactory.newLatLngZoom(mMarkerOptions.getPosition(), 14) );




        database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Favorite Places");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String latitude = (String) snapshot.child("latitude").getValue();
                    String longitude = (String) snapshot.child("longitude").getValue();
                    double lat = Double.valueOf(latitude);
                    double lon = Double.valueOf(longitude);
                    name = (String) snapshot.child("name").getValue();



                    //Toast.makeText(MapsActivity.this, Places.get(0), Toast.LENGTH_SHORT).show();



                    //mDestination = new LatLng(lat,lon);

                    //mMarkerOptionsDest2 = new MarkerOptions( ).position(new LatLng(lat,lon) ).title(name);
                    // Add new marker to the Google Map Android API V2.
                    //mMap.addMarker( mMarkerOptionsDest2 );
                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lon)).title(name));

                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            mDestination = marker.getPosition();
                            mMarkerOptionsDest2 = new MarkerOptions( ).position(marker.getPosition()).title(marker.getTitle());
                            if ( mOrigin != null && mDestination != null )  {
                                //drawRoute( );
                                calculateDirections(mMarkerOptionsDest2,mOrigin);
                            }

                            return false;
                        }
                    });



                    //String snippet;
                    //mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lon)).title(name));



                    String name2 = (String) snapshot.child("name").getValue();
                    Log.v("TAG","Name is: "+ name2);
                    Places.add(name2);
                    adapter.notifyDataSetChanged();

                }

            }





            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });










/**
            mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
               @Override
                public void onPolylineClick(Polyline polyline) {
                    Toast.makeText(MapsActivity.this, "Clicked Polyline", Toast.LENGTH_SHORT).show();

                   for(PolyLine polylineData: PolyLines){
                       Log.d("TAG", "onPolylineClick: toString: " + polylineData.toString());
                       if(polyline.getId().equals(polylineData.getPolyline().getId())){
                           polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.Green));
                           polylineData.getPolyline().setZIndex(1);

                           //mMarkerOptionsDest = new MarkerOptions( ).position( mDestination ).title( "Destination" ).snippet("Time Taken: "+ polylineData.getLeg().duration);
                           // Add new marker to the Google Map Android API V2.
                           //mMap.addMarker( mMarkerOptionsDest );

                           Marker marker = mMap.addMarker(new MarkerOptions().position(mDestination).title("Destination").snippet("Time Taken: "+ polylineData.getLeg().duration));
                           marker.showInfoWindow();

                       }
                       else{
                           polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGrey));
                           polylineData.getPolyline().setZIndex(0);
                       }
                   }
                }
            }); **/




        //locationManager.requestLocationUpdates( bestProvider, 20000, 0, this );



/**
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));**/




    }

    private void getMyLocation( ) {
        // Getting LocationManager object from System Service LOCATION_SERVICE
        mLocationManager = (LocationManager) getSystemService( LOCATION_SERVICE );

        mLocationListener = new LocationListener( ) {

            @Override
            public void onLocationChanged( Location location ) {
                //Toast.makeText(MapsActivity.this, "Location changed", Toast.LENGTH_SHORT).show();
                mOrigin = new LatLng( location.getLatitude( ), location.getLongitude( ) );
                mMarkerOptions = new MarkerOptions( ).position( mOrigin ).title( "Current Location" );
                mMap.addMarker(mMarkerOptions);
                //adapter.notifyDataSetChanged();
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(mOrigin));
                //mMap.animateCamera( CameraUpdateFactory.zoomTo(14), 2000, null );

                //mDestination = new LatLng( 47.9229, -97.;



                //if ( mOrigin != null && mDestination != null )
                //{//drawRoute( );
                   // calculateDirections(mMarkerOptionsDest,mOrigin);
                //}
            }

            @Override
            public void onStatusChanged( String provider, int status, Bundle extras ) {
                // TODO Auto-generated method stub
            }  // End of onStatusChanged

            @Override
            public void onProviderEnabled( String provider ) {
                // TODO Auto-generated method stub
            }  // End of onProviderEnabled

            @Override
            public void onProviderDisabled( String provider ) {
                // TODO Auto-generated method stub
            }  // End of onProviderDisabled

        };  // End of LocationListener

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, mLocationListener);

        mMap.setOnMapLongClickListener( new GoogleMap.OnMapLongClickListener( ) {

            @Override
            public void onMapLongClick( LatLng latLng ) {
                mDestination = latLng;
                mMap.clear( );

                mMarkerOptionsDest2 = new MarkerOptions( ).position(mDestination).title("Destination");

                mMarkerOptions = new MarkerOptions( ).position( mOrigin ).title( "Current Location" );
                mMap.addMarker(mMarkerOptions);
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(mOrigin));
                //mMap.animateCamera( CameraUpdateFactory.zoomTo(14), 2000, null );
                mMap.animateCamera( CameraUpdateFactory.newLatLngZoom(mMarkerOptions.getPosition(), 14) );

                mMarkerOptionsDest = new MarkerOptions( ).position( mDestination ).title( "Destination" );
                // Add new marker to the Google Map Android API V2.
                mMap.addMarker( mMarkerOptionsDest );
                if ( mOrigin != null && mDestination != null )  {
                    //drawRoute( );
                    calculateDirections(mMarkerOptionsDest,mOrigin);
                }
            }  // End of onMapLongClick

        } );  // End of setOnMapLongClickListener


    }  // End of getMyLocation

    private boolean isGooglePlayServicesAvailable( ) {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable( this );
        if ( ConnectionResult.SUCCESS == status ) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog( status, this, 0 ).show( );
            return false;
        }
    }


    private boolean verifyAllPermissions( int[ ] grantResults ) {
        for ( int result : grantResults )
            if ( result != PackageManager.PERMISSION_GRANTED )
                return false;
        return true;
    }  // End of verifyAllPermissions



    private void drawRoute( ) {
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl( mOrigin, mDestination );
        DownloadTask downloadTask = new DownloadTask( );
        // Start downloading json data from Google Directions API.
        downloadTask.execute( url );
    }  // End of drawRoute


    private String getDirectionsUrl( LatLng origin, LatLng dest ) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String key = "key=" + getString( R.string.google_maps_key );
        String parameters = str_origin + "&" + str_dest + "&" + key;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }


    // A method to download json data from url
    private String downloadUrl( String strUrl ) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL( strUrl );
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection( );
            // Connecting to URL
            urlConnection.connect( );
            // Reading data from URL
            iStream = urlConnection.getInputStream( );
            BufferedReader br =
                    new BufferedReader( new InputStreamReader( iStream ) );
            StringBuffer sb = new StringBuffer( );
            String line = "";
            while( ( line = br.readLine( ) ) != null )  sb.append( line );
            data = sb.toString( );
            br.close( );
        }  // End of try
        catch( Exception e ) {
            Log.d( "Exception on download", e.toString( ) );
        }
        finally {
            iStream.close( );
            urlConnection.disconnect( );
        }
        return data;
    }  // End of downloadUrl

    @Override
    public void onPolylineClick(Polyline polyline) {

        Toast.makeText(MapsActivity.this, "Clicked Polyline", Toast.LENGTH_SHORT).show();

        for(PolyLine polylineData: PolyLines){

            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.Green));
                polylineData.getPolyline().setZIndex(1);

                //mMarkerOptionsDest = new MarkerOptions( ).position( mDestination ).title( "Destination" ).snippet("Time Taken: "+ polylineData.getLeg().duration);
                // Add new marker to the Google Map Android API V2.
                //mMap.addMarker( mMarkerOptionsDest );

                Marker marker = mMap.addMarker(new MarkerOptions().position(mDestination).title(mMarkerOptionsDest2.getTitle()).snippet("Time Taken: "+ polylineData.getLeg().duration));
                mMarkerOptionsDest2.title("Destination");
                marker.showInfoWindow();

            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }

    }


    // A class to download data from Google Directions URL
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground( String... url ) {
            // For storing data from web service
            String data = "";
            try {
                // Fetching the data from web service
                data = downloadUrl( url[0] );
                Log.d( "DownloadTask", "DownloadTask : " + data );
            }
            catch( Exception e ) {
                Log.d( "Background Task", e.toString( ) );
            }
            return data;
        }  // End of doInBackground


        // Executes in UI thread, after the execution of doInBackground( )
        @Override
        protected void onPostExecute( String result ) {
            super.onPostExecute( result );
            ParserTask parserTask = new ParserTask( );
            // Invokes the thread for parsing the JSON data
            parserTask.execute( result );
        }  // End of onPostExecute

    }  // End of DownloadTask


    // A class to parse the Google Directions in JSON format
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground( String... jsonData ) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject( jsonData[0] );
                DirectionsJSONParser parser = new DirectionsJSONParser();
                // Starts parsing data.
                routes = parser.parse( jObject );
            }
            catch( Exception e ) {
                e.printStackTrace();
            }
            return routes;
        }  // End of doInBackground


        // Executes in UI thread, after the parsing process.
        @Override
        protected void onPostExecute( List<List<HashMap<String, String>>> result ) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for ( int i=0; i<result.size( ); i++ ) {
                points = new ArrayList<LatLng>( );
                lineOptions = new PolylineOptions( );
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get( i );

                // Fetching all the points in i-th route
                for ( int j=0; j<path.size( ); j++ ) {
                    HashMap<String,String> point = path.get( j );
                    double lat = Double.parseDouble( point.get( "lat" ) );
                    double lng = Double.parseDouble( point.get( "lng" ) );
                    LatLng position = new LatLng( lat, lng );
                    points.add( position );
                }  // End of inner for

                // Adding all the points in the route to LineOptions
                lineOptions.addAll( points );
                lineOptions.width( 2 );
                lineOptions.color( Color.RED );
            }  // End of outer for

            // Drawing polyline in the Google Map for the i-th route
            if ( lineOptions != null ) {
                if ( mPolyline != null )  mPolyline.remove( );
                mPolyline = mMap.addPolyline( lineOptions );
            }
            else
                Toast.makeText( getApplicationContext( ), "No route is found",
                        Toast.LENGTH_LONG ).show( );

        }  // End of onPostExecute

    }  // End of ParserTask

    @Override
    // This method is invoked for every call on requestPermissions.
    public void onRequestPermissionsResult( int requestCode,
                                            String[ ] permissions, int[ ] grantResults ) {
        if ( requestCode == 100 )
            if ( !verifyAllPermissions( grantResults ) )
                Toast.makeText( getApplicationContext( ), "No sufficient permissions",
                        Toast.LENGTH_LONG ).show( );
            else{
                getMyLocation( );
    }
        else
            super.onRequestPermissionsResult( requestCode, permissions, grantResults );
    }  // End of onRequestPermissionsResult


}
