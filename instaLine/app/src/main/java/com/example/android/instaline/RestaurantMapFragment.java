package com.example.android.instaline;


import android.content.Context;
import android.content.res.Resources;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.internal.gmsg.HttpClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Address;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantMapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private Context mContext;
    private MapView mMapView;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private String address;


    public RestaurantMapFragment() {
        // Required empty public constructor
    }

    public static RestaurantMapFragment newInstance() {
        RestaurantMapFragment fragment = new RestaurantMapFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        String originalAddress = getArguments().getString("address");
//        String originalAddress = savedInstanceState.getString("address");
        address = replace(originalAddress);

        View view = inflater.inflate(R.layout.fragment_restaurant_map, container, false);
        return view;
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
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.restaurant_map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
        String link = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&sensor=false";
        System.out.println(link);

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

        mMap.addMarker(new MarkerOptions().position(resLatLng).title("Destination"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(resLatLng, 15.0f));

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
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream is = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(is);

                int data = inputStreamReader.read();
                while(data != -1){
                    char curr = (char) data;
                    result += curr;
                    data = inputStreamReader.read();
                }
//                System.out.println(result);
                return result;

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
