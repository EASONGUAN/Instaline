package com.example.android.instaline;


import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private MapView mMapView;

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private static int UPDATE_INTERVAL = 1000;
    private static int FASTEST_INTERVAL = 1000;
    private static int DISPLACEMENT = 10;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    Marker mCurrent;

    private Context mContext;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_map, container, false);
//
//        SupportMapFragment mapFragment = (SupportMapFragment)getFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//
//        return view;
        return inflater.inflate(R.layout.fragment_map, null);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try{
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(mContext, R.raw.uber_map_style)
            );
            if(!isSuccess){
                System.out.println("GOOGLE MAP: Invalid map style ************************* ");
            }
        } catch(Resources.NotFoundException ex){
            ex.printStackTrace();
        }

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
            Toast.makeText(mContext, "This device is not supported", Toast.LENGTH_SHORT).show();
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }

        // SET MARKER of EVERY RESTAURANt
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference restRef = databaseReference.child("Restaurant");
        restRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot);
                for (DataSnapshot addressSnapshot: dataSnapshot.getChildren()) {
                    String cur_address = addressSnapshot.child("address").getValue(String.class);
                    String resName = addressSnapshot.child("name").getValue(String.class);
                    String address = replace(cur_address);
                    String link = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&sensor=false";
                    GetLocationDownloadTask getLocation = new GetLocationDownloadTask();
                    JSONObject locationObject = null;
                    try {
                        String result = getLocation.execute(link).get();
                        try {
                            locationObject = new JSONObject(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (java.lang.NullPointerException e){
                            e.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    LatLng resLatLng = getLatLng(locationObject);
                    mMap.addMarker(new MarkerOptions().position(resLatLng).title(resName));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        System.out.println("GOOGLE MAP: Client Build status ************************* :" + mGoogleApiClient);
    }

    protected synchronized void displayLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
            Toast.makeText(mContext, "This device is not supported", Toast.LENGTH_SHORT).show();
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        System.out.println("Last location at displayLocation ************************* :" + mLastLocation);
        if(mLastLocation != null){
            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();

            // Update to Firebase LATER ********* TODO *********

            // Update Marker
            if(mCurrent != null){
                mCurrent.remove();
            }
            //mCurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title(String.format("You")));
            System.out.println("CREATING MARKER ***********************\n");

            // move to this position
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),15.0f));
            // Draw animation
//            rotateMarker(mCurrent, -360, mMap);
            Log.d("EDMTDEV", String.format("new location: %f / %f", latitude, longitude));
            System.out.println("SUCCESS ****************************** Get current location\n");
        } else{
            System.out.println("Requesting new location at displayLocation ************************* :" + mLastLocation);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        System.out.println("GOOGLE MAP: location at onLocationChanged status ************************* :" + mLastLocation);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); // user location center in map (every update)
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            // Create the LocationRequest object
            System.out.println("GOOGLE MAP: location at onConnected status ************************* :" + mLastLocation);
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setSmallestDisplacement(DISPLACEMENT)
                    .setInterval(UPDATE_INTERVAL)
                    .setFastestInterval(FASTEST_INTERVAL);
            System.out.println("GOOGLE MAP: onConnected myLocationRequest Status *************************: " + mLocationRequest);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            System.out.println("GOOGLE MAP: onConnected Status *************************: Requesting new location");
        }else{
            displayLocation();
            System.out.println("GOOGLE MAP: last location at onConnected status ************************* :" + location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("GOOGLE MAP: Connection Suspended ************************* ");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("GOOGLE MAP: Connection Failed ************************* ");

    }

    private boolean checkPlayServices(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
//                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
                Toast.makeText(mContext, "This device is not supported", Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(mContext, "This device is not supported", Toast.LENGTH_SHORT).show();
//                finish();
                return false;
            }
            return false;
        }
        return true;
    }

    public LatLng getLatLng(JSONObject jsonObject) {
        Double lon = new Double(0);
        Double lat = new Double(0);
        try {
            lon = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");

            lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (java.lang.NullPointerException e){
            e.printStackTrace();
        }
        return new LatLng(lat, lon);

    }

    public String replace(String str) {
        return str.replaceAll(" ", "%20");
    }

    public class GetLocationDownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result = "";

            URL url;
            HttpURLConnection urlConnection;
            try {
//                url = new URL(strings[0]);
//                urlConnection = (HttpURLConnection) url.openConnection();
//                InputStream is = urlConnection.getInputStream();
//                InputStreamReader inputStreamReader = new InputStreamReader(is);
//
//                int data = inputStreamReader.read();
//                while(data != -1){
//                    char curr = (char) data;
//                    result += curr;
//                    data = inputStreamReader.read();
//                    System.out.println(data);
//                }

                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream is = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(is);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuffer stringBuffer = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuffer.append(line);
                }

                return stringBuffer.toString();

//                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result != null) {
                try {
                    JSONObject locationObject = new JSONObject(result);
                    JSONObject locationGeo = locationObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
