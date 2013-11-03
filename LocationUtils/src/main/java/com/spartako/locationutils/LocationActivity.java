package com.spartako.locationutils;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public abstract class LocationActivity extends Activity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private LocationClient mLocationClient;
    private boolean mConnected = false;
    private boolean mLocationUpdatesHaveBeenRequested = false;
    private Location mLastFix;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            setLastFix(location);
            onNewLocationFix();
        }
    };

    /**
     * <p>Will be called every time there is a new location fix available.</p>
     * <p>Location fixes will arrive following the criteria defined in
     * {@link #getLocationRequest()}.</p>
     */
    abstract protected void onNewLocationFix();

    /**
     * <p>Defines the criteria for location fixes.</p>
     * <p>Will be called every time {@link #startLocationUpdates()} is called, in case the criteria
     * has changed.</p>
     *
     * @return LocationRequest: The criteria for the location fixes.
     */
    abstract protected LocationRequest getLocationRequest();

    /*PUBLIC METHODS*/

    /**
     * <p>Start receiving location updates using the criteria defined in
     * {@link #getLocationRequest()}.</p>
     *
     * <p>If no criteria is provided ({@link #getLocationRequest()} returns null) this method will
     * not have any effect.</p>
     *
     * <p>If the location service is not yet available the location updates will start when it is
     * ready.</p>
     */
    public void startLocationUpdates() {
        if (getLocationRequest() != null) {
            if (isConnected()) {
                requestLocationUpdates();
            } else {
                requestLocationUpdatesWhenReady();
            }
        }
    }

    /**
     * Stop receiving location updates.
     */
    public void stopLocationUpdates() {
        removeLocationUpdates();
    }

    /**
     * <p>Get the last location fix.</p>
     *
     * @return Location: The last location fix, or null if there is none.
     */
    public Location getLastFix() {
        return mLastFix;
    }

    /*ACTIVITY LIFECYCLE*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mLocationClient == null) {
            if (servicesConnected()) {
                mLocationClient = new LocationClient(this, this, this);
            }
        }
    }

    @Override
    protected void onStart() {
        // Connect the client.
        mLocationClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    /* LOCATION RELATED METHODS*/

    /*
     * Handle results returned to the Activity by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                // If the result code is Activity.RESULT_OK, try to connect again
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        //Try the request again
                        mLocationClient.connect();
                        break;
                }
        }
    }

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Called by Location Services when the request to connect the client finishes successfully.
     * At this point, you can request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        setConnected();
        if (isThereLocationUpdatesRequested())
            requestLocationUpdates();
    }

    /*
     * Called by Location Services if the connection to the location client drops because of an
     * error.
     */
    @Override
    public void onDisconnected() {
        setDisconnected();
        // NOTE should notify subclasses that the client has been disconnected for some reason.
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        setDisconnected();
        // Google Play services can resolve some errors it detects. If the error has a resolution,
        // try sending an Intent to start a Google Play services activity that can resolve the
        // error.
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                // Thrown if Google Play services canceled the original PendingIntent
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /* Methods to manage Google PS availability*/
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int errorCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (errorCode != ConnectionResult.SUCCESS) {
            showErrorDialog(errorCode);
            return false;
        } else return true;
    }

    private void showErrorDialog(int errorCode) {
        GooglePlayServicesUtil.getErrorDialog(errorCode, this, 0).show();
        /*new AlertDialog.Builder(this)
                .setMessage(String.valueOf(errorCode))
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).show();*/
    }

    private void requestLocationUpdates() {
        mLocationClient.requestLocationUpdates(getLocationRequest(), getLocationListener());
        if (isThereLocationUpdatesRequested()) requestedLocationUpdatesCompleted();

    }

    private void removeLocationUpdates() {
        mLocationClient.removeLocationUpdates(getLocationListener());
    }

    private LocationListener getLocationListener() {
        return this.mLocationListener;
    }

    private void requestLocationUpdatesWhenReady() {
        this.mLocationUpdatesHaveBeenRequested = true;
    }

    private void requestedLocationUpdatesCompleted() {
        this.mLocationUpdatesHaveBeenRequested = false;
    }

    private boolean isThereLocationUpdatesRequested() {
        return mLocationUpdatesHaveBeenRequested;
    }

    private void setConnected() {
        this.mConnected = true;
    }

    private void setDisconnected() {
        this.mConnected = false;
    }

    private boolean isConnected() {
        return mConnected;
    }

    private void setLastFix(Location location) {
        this.mLastFix = location;
    }


}

