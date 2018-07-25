package com.example.nilotpal.myapp;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

//import android.location.LocationListener;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final long INTERVAl = 1000*2;
    private static final long FASTEST_INTERVAL = 1000*1;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation,lStart,lEnd;

    static double distance = 0;
    double speed;

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
        return mBinder;
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAl);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startID){
        return super.onStartCommand(intent, flag, startID);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }catch (SecurityException e) {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location){
        MainActivity.locate.dismiss();       // ???
        mCurrentLocation = location;
        if(lStart==null){
            lStart = lEnd = mCurrentLocation;
        }
        else{
            lEnd = mCurrentLocation;
        }

        //Update information
        updateUI();

        // speed calculation
        speed = location.getSpeed() * 18 / 5;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }



    private void updateUI() {
        if(MainActivity.p == 0){
            distance = distance + (lStart.distanceTo(lEnd)/1000.00);
            MainActivity.endTime = System.currentTimeMillis();
            long diff = MainActivity.endTime - MainActivity.startTime;
            diff = TimeUnit.MILLISECONDS.toMinutes(diff);
            MainActivity.time.setText("Total Time: " + diff + " minutes");
            if(speed > 0.0) {
                MainActivity.speed.setText("Instantaneous Speed: " + new DecimalFormat("#.##").format(speed) + " km/hr");
            }
            else {
                MainActivity.speed.setText("....");
            }
            MainActivity.distance.setText("Total Distance: " + new DecimalFormat("#.###").format(distance) + " km's");
            lStart = lEnd;
        }

    }

    @Override
    public boolean onUnbind(Intent intent){
        stopLocationUpdates();
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
        lStart = null;
        lEnd = null;
        distance = 0;
        return super.onUnbind(intent);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        distance = 0;
    }


}
