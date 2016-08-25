package com.tuananhcaraballo.Pyxis;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {


    // note to check pushing commits
    private TextView latitudeText;
    private TextView longitudeText;
    private TextView altitudeText;
    private TextView addressText;

    private TextView average_temperature;
    private TextView min_temperature;
    private TextView max_temperature;
    private TextView weather_description;
    private ImageView weather_icon;


   // private TextView latitude_longitude;  //---> used for the new way to get latitude and longitude in the intentService
    private Button fetchButton;
    private Button switchMap;
    private MapFragment mapFragment;
    private ProgressBar mapProgress_bar;
    private ProgressBar progressBar;

    private FusedLocationProviderApi locationProviderApi = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;


    private Double Latitude = 37.4295055;
    private Double Longitude = -122.1647152;

    //--------- Weather Variables:






    private String TAG = "-- MainActivity --";


    // --- Variables Required for Reverse Geolocation

    //--> not really necessary for this code, it's the same location as the one
    // passed as a parameter to the function on Location Changed
    protected Location lastLocation;
    protected boolean isAddressRequested;
    protected String addressOutput;
    private AddressResultReceiver resultReceiver;
    private WeatherResultReceiver weatherResultReceiver;

    protected boolean isMapRequested;

    protected static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";

    private GoogleMap myGoogleMap;

//    @Override
//    protected  void onPreExecute(){
//        switcher = new ViewSwitcher(MainActivity.this);
//        switcher.addView(View.inflate(MainActivity.this,R.layout.loading_screen,null));
//        setContentView(switcher);
//    }


    private Animation fade_in, fade_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setGlobalVariables();
        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();
        setLocationRequest();
        setMapFragment();
        updateUI();


    }

    private void setMapFragment() {
        Log.wtf(TAG,"SET MAP FRAGMENT");
        mapFragment.getMapAsync(this);
    }


    private void setGlobalVariables() {
        latitudeText = (TextView) findViewById(R.id.tvLatitude);
        longitudeText = (TextView) findViewById(R.id.tvLongitude);
        altitudeText = (TextView) findViewById(R.id.tvAltitude);
        addressText = (TextView) findViewById(R.id.tvAddress);
        fetchButton = (Button) findViewById(R.id.fetchButton);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        switchMap = (Button) findViewById(R.id.switchMap);
        //latitude_longitude = (TextView) findViewById(R.id.new_Lat_Long);
        mapProgress_bar = (ProgressBar) findViewById(R.id.mapprogress_bar);

        weather_icon = (ImageView) findViewById(R.id.weather_icon);
        weather_description = (TextView) findViewById(R.id.weather_description);
        average_temperature = (TextView) findViewById(R.id.ave_temp);
        min_temperature = (TextView) findViewById(R.id.min_temp);
        max_temperature = (TextView) findViewById(R.id.max_temp);



        //-- required variables for reverse GeoLocation:
        resultReceiver = new AddressResultReceiver(new Handler());
        weatherResultReceiver = new WeatherResultReceiver(new Handler());
        isAddressRequested = false;
        isMapRequested = false;
        addressOutput = "";
    }

    private void updateUI() {
        if (lastLocation != null) {
            Latitude = lastLocation.getLatitude();
            Longitude = lastLocation.getLongitude();


            Double Altitude = lastLocation.getAltitude();
            latitudeText.setText("Latitude is: " + String.valueOf(Latitude));
            longitudeText.setText("Longitude is: " + String.valueOf(Longitude));

            if (Altitude == 0.0) {
                altitudeText.setVisibility(View.GONE);
                //altitudeText.setText("Altitude is: sorry, not available at this time");
            } else {
                altitudeText.setText("Altitue is: " + String.valueOf(Altitude));
            }

            updateAddressUI();
            // updateMapUI(1);

        }
    }

    private void updateAddressUI(){
        if (isAddressRequested) { //  -->  is address has been requested then show the progress bar
            progressBar.setVisibility(ProgressBar.VISIBLE);
            fetchButton.setEnabled(false);
        } else {   // --> otherwise if has not been requested, or if the address has already been returned
            // by the service hide the progressbar
            progressBar.setVisibility(ProgressBar.GONE);
            fetchButton.setEnabled(true);
        }
    }

    protected void displayAddressOutput() {
        addressText.setText(addressOutput);
    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void setLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10 * 1000); // we don't want to do this too often
        locationRequest.setFastestInterval(5 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); // this is prefered
        // because it saves power
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.v(TAG, "onConnected() YES CONNECTED SUCCESSFULLY ");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        lastLocation = locationProviderApi.getLastLocation(googleApiClient);

        Latitude = lastLocation.getLatitude();
        Longitude = lastLocation.getLongitude();
        //moveTheCamera(Latitude,Longitude);
        startIntentService();
        startWeatherService();
        isAddressRequested = true;
        updateAddressUI();
        myGoogleMap.clear();
        moveTheCamera(Latitude,Longitude);


        requestLocationUpdates();

        if(lastLocation != null){
            if(!Geocoder.isPresent()){
                Toast.makeText(this, "No Geocoder available", Toast.LENGTH_LONG).show();
                return;
            }
            if(isAddressRequested){
                startIntentService();
            }
        }


    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.v(TAG, "requestLocationUpdates() trying  adifferent approach ");
        locationProviderApi.requestLocationUpdates(googleApiClient,locationRequest,this);
    }


    private void updateValuesFromBundle(Bundle savedInstanceState){
        if(savedInstanceState != null){
            if(savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)){
                isAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            if(savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)){
                addressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                displayAddressOutput();
            }

            if(savedInstanceState.keySet().contains(Constants.LATITUDE) &&
                    savedInstanceState.keySet().contains(Constants.LONGITUDE)  ){
                Latitude = savedInstanceState.getDouble(Constants.LATITUDE);
                Longitude = savedInstanceState.getDouble(Constants.LONGITUDE);
                Log.v(TAG, "THE LATITUDE AND LONGITUDE WERE SAVED! " + Latitude.toString());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY,isAddressRequested);
        savedInstanceState.putString(LOCATION_ADDRESS_KEY,addressOutput);
        savedInstanceState.putDouble(Constants.LATITUDE, Latitude);
        savedInstanceState.putDouble(Constants.LONGITUDE, Longitude);
        mapFragment.setRetainInstance(true);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        updateValuesFromBundle(savedInstanceState);


        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG, "onConnectionSuspended() connection was suspended");
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG, "onConnectionFailed() connection FAILED");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v(TAG, "onLocationChanged() ");
        lastLocation = location;
        updateUI();

    }
    protected void startIntentService(){
        Intent intent = new Intent(this, FetchAddressIntentService.class); //--> need to create this cla
        intent.putExtra(Constants.RECEIVER,resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, lastLocation);
        startService(intent);
    }

    protected void startWeatherService(){
        Intent intent = new Intent(this, FetchWeatherService.class); //--> need to create this cla
        intent.putExtra(Constants.RECEIVER_WEATHER, weatherResultReceiver);
        intent.putExtra(Constants.LATITUDE, Latitude.toString());
        intent.putExtra(Constants.LONGITUDE, Longitude.toString());
        startService(intent);
    }

    /// ------------- venturing in creating a service for Google maps:
    /*
        1- create a resultReceiver
        2- create the function StartGoogleMapsIntentService, you will the LATITUDE AND LONGITUDE
           as extras in the intent, or pass it the lastLocation object
        3- create the call back function, what would you do after your googleMap service returns
        4- Create the class that will do with calling googleMaps
        5- Add the service to te Manifest file
        6- Start the service in onConnected or onLocationChanged


     */
//    protected void startGoogleMapsIntentService(){
//        Intent intent = new Intent(this, FetchGoogleMapIntentService.class); //--> need to create this cla
//        intent.putExtra(Constants.RECEIVER,resultReceiver);
//        intent.putExtra(Constants.LOCATION_DATA_EXTRA, lastLocation);
//        startService(intent);
//    }


    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        // Log.v(TAG, "onStart()  successful googleApiClient connection:  " + googleApiClient.toString());


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(googleApiClient.isConnected()){
            requestLocationUpdates();
            Log.v(TAG, "onResume() googleApiIs connected");
        }else{
            Log.v(TAG, "onResume() googleApiIs  is not connected");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(googleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        }else{
            googleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    public void fetchButtonHandler(View view) {



        if(googleApiClient.isConnected() && lastLocation != null){
            Log.v(TAG,"GOOGLE API IS CONNECTED AND LOCATION IS NOT NULL SERVICE WILL START!");
            moveTheCamera(Latitude,Longitude);
            startIntentService();
        }else{
            if(!googleApiClient.isConnected() && lastLocation != null){
                Log.v(TAG,"GOOGLE API IS NOT CONNECTED");
            }
            if(lastLocation == null){
                Log.v(TAG,"THE LOCATION IS NULL!!");
            }

        }
        Log.v(TAG, " ------------- >  fetchButtonHandler() ");
        isAddressRequested = true;
       // isMapRequested = true;

        updateUI();
       // updateMapUI(1);
    }

    private void updateMapUI(int code){
        Log.v("-------> UPDATE MAP UI", "the code: " + code);
        if(isMapRequested){
            mapProgress_bar.setVisibility(ProgressBar.VISIBLE);
            mapFragment.getView().setVisibility(View.GONE);
        }else {
            progressBar.setVisibility(ProgressBar.GONE);
            mapFragment.getView().setVisibility(View.VISIBLE);
        }
    }

    private void moveTheCamera(Double Latitude, Double Longitude){
        LatLng myLocation = new LatLng(Latitude, Longitude);
        myGoogleMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));
        myGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        myGoogleMap.addMarker(new MarkerOptions().position(new LatLng(Latitude, Longitude)).title("Marker"));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLocation)
                .zoom(17)
                .bearing(90)
                .tilt(30)
                .build();
        myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    // NOTE: this is the callback function that gets called when the method is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.wtf("----> onMapReady()","MAP IS READY");
        myGoogleMap = googleMap;
        isMapRequested = false;
        updateMapUI(2);
        moveTheCamera(Latitude,Longitude);
    }

    public void changeMap(View view) {
        if (myGoogleMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
            myGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            switchMap.setText("Normal View");
        } else{
            myGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            switchMap.setText("Earth View");
        }
    }




    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver{
        public AddressResultReceiver(Handler handler){
            super(handler);
        }

        @Override
        protected  void onReceiveResult(int resultCode, Bundle resultData){
            Log.v(TAG, "GOT TO ONRECEIVERESULT!!");
            addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            Log.v(TAG, "THE ADDRESS OUTPUT IS: " + addressOutput);
            displayAddressOutput();
            //display data here, displayadderss output
            isAddressRequested = false;
            updateAddressUI();   // --> got read of updateUI because we only need to update
                                // the address UI, calling updateUI will run code we don't need.
        }
    }




    @SuppressLint("ParcelCreator")
    class WeatherResultReceiver extends  ResultReceiver{

        public WeatherResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.wtf(TAG, "Got to Weather Result receiver on WeatherResultReceiver");
            String Description = resultData.getString(Constants.WEATHER_DESCRIPTION);
            if(resultCode == Constants.SUCCESS_RESULT){
                int Max_Temp = resultData.getInt(Constants.MAX_TEMP);
                int Min_Temp = resultData.getInt(Constants.MIN_TEMP);
                int Average_Temp = resultData.getInt(Constants.AVERAGE_TEMP);
                String Weather_Icon = resultData.getString(Constants.WEATHER_ICON);
                Log.wtf(TAG, "Min_Temp: "+ Min_Temp);
                Log.wtf(TAG, "Max_Temp: "+ Max_Temp);
                Log.wtf(TAG, "Ave_Temp: "+ Average_Temp);
                Log.wtf(TAG, "Weather_Icon: "+ Weather_Icon);

                average_temperature.setText(String.valueOf(Average_Temp));
                min_temperature.setText(String.valueOf(Min_Temp));
                max_temperature.setText(String.valueOf(Max_Temp));
                weather_description.setText(Description);

                String IconHTTPURL = "http://openweathermap.org/img/w/"+Weather_Icon+".png";
                Picasso.with(getBaseContext()).load(IconHTTPURL).into(weather_icon);


            }

            Log.wtf(TAG, "Description: "+ Description);

            // update the MapUI





        }
    }
}