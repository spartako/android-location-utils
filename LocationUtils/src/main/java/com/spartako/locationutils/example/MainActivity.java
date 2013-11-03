package com.spartako.locationutils.example;

import android.os.Bundle;
import android.text.Editable;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.spartako.locationutils.LocationActivity;
import com.spartako.locationutils.R;

public class MainActivity extends LocationActivity {
    /* One second in ms*/
    private static final long ONE_SECOND = 1000;
    private static final long REFRESH_DELAY = ONE_SECOND;
    private static final String KEY_LOCATION_REQUEST = "com.spartako.locationutils.example.KEY_LOCATION_REQUEST";
    private static final String KEY_LOCATION_REQUEST_ON_GOING = "com.spartako.locationutils.example.KEY_LOCATION_REQUEST_ON_GOING";

    private LocationRequest mLocationRequest;
    private Time mLastFixTime = new Time();
    private boolean mRequestUpdatesOnGoing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findAllViews();
        setUpButtonListeners();
        if (savedInstanceState == null) {
            mLocationRequest = defaultRequest();
            fillDefaults();
        }
    }

    @Override
    protected void onNewLocationFix() {
        refreshInformationTextViews();
        Toast.makeText(this, getString(R.string.new_fix_exclamation), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected LocationRequest getLocationRequest() {
        return this.mLocationRequest;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(KEY_LOCATION_REQUEST)) {
            mLocationRequest = savedInstanceState.getParcelable(KEY_LOCATION_REQUEST);
        }else mLocationRequest = defaultRequest();
        if (savedInstanceState.containsKey(KEY_LOCATION_REQUEST_ON_GOING)) {
            if (savedInstanceState.getBoolean(KEY_LOCATION_REQUEST_ON_GOING)) {
                MainActivity.this.startLocationUpdates();
                mLastFixTimeView.setText(getString(R.string.waiting_for_fix));
                mRequestUpdatesOnGoing = true;
                mStartButton.setEnabled(false);
                mStopButton.setEnabled(true);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getLocationRequest() != null) {
            outState.putParcelable(KEY_LOCATION_REQUEST, mLocationRequest);
        }
        outState.putBoolean(KEY_LOCATION_REQUEST_ON_GOING, mRequestUpdatesOnGoing);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //remove looping handler
        mTimeSinceLastFixView.removeCallbacks(refreshTimeSinceLastFixView);
    }

    private LocationRequest defaultRequest() {
        LocationRequest defaultRequest = new LocationRequest();
        defaultRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        defaultRequest.setInterval(ONE_SECOND * 10);
        defaultRequest.setFastestInterval(ONE_SECOND * 8);
        return defaultRequest;
    }

    private void fillDefaults() {
        //BALANCED_POWER_ACCURACY (second position in the array)
        mPrioritySpinner.setSelection(1);
        mIntervalEditText.setText(String.valueOf(mLocationRequest.getInterval()));
        mFastestIntervalEditText.setText(String.valueOf(mLocationRequest.getFastestInterval()));
    }

    private void setUpButtonListeners() {
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRequestUpdatesOnGoing) {
                    convertInputsToLocationRequest();
                    MainActivity.this.startLocationUpdates();
                    mLastFixTimeView.setText(getString(R.string.waiting_for_fix));
                    mRequestUpdatesOnGoing = true;
                    mStartButton.setEnabled(false);
                    mStopButton.setEnabled(true);
                }
            }
        });
        mStopButton.setEnabled(false);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.stopLocationUpdates();
                clearInformationTextViews();
                mRequestUpdatesOnGoing = false;
                mStartButton.setEnabled(true);
                mStopButton.setEnabled(false);
            }
        });

    }

    private void convertInputsToLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        String priority = (String) mPrioritySpinner.getSelectedItem();
        if (priority != null) {
            if (priority.equals("HIGH_ACCURACY")) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            } else if (priority.equals("BALANCED_POWER_ACCURACY")) {
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            } else if (priority.equals("LOW_POWER")) {
                locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
            } else if (priority.equals("NO_POWER")) {
                locationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
            }
        }
        if (validateMillis(mIntervalEditText.getText())) {
            locationRequest.setInterval(Long.valueOf(mIntervalEditText.getText().toString()));
        }
        if (validateMillis(mFastestIntervalEditText.getText())) {
            locationRequest.setFastestInterval(Long.valueOf(mFastestIntervalEditText.getText().toString()));
        }
        if (validateMillis(mExpirationTimeEditText.getText())) {
            locationRequest.setExpirationTime(Long.valueOf(mExpirationTimeEditText.getText().toString()));
        }
        if (validateMillis(mExpirationDurationEditText.getText())) {
            locationRequest.setExpirationDuration(Long.valueOf(mExpirationDurationEditText.getText().toString()));
        }
        if (validateNumber(mNumUpdatesEditText.getText())) {
            locationRequest.setNumUpdates(Integer.valueOf(mNumUpdatesEditText.getText().toString()));
        }
        if (validateMeters(mSmallestDisplacementEditText.getText())) {
            locationRequest.setSmallestDisplacement(Math.abs(Float.valueOf(mSmallestDisplacementEditText.getText().toString())));
        }

        this.mLocationRequest = locationRequest;
    }

    private boolean validateNumber(Editable number) {
        if (number != null) {
            try {
                if (Integer.valueOf(number.toString()) <= 0) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private boolean validateMeters(Editable meters) {
        if (meters != null) {
            try {
                Float.valueOf(meters.toString());
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private boolean validateMillis(Editable millis) {
        if (millis != null) {
            try {
                if (Long.valueOf(millis.toString()) <= 0) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private void findAllViews() {
        mStartButton = (Button) findViewById(R.id.startButton);
        mStopButton = (Button) findViewById(R.id.stopButton);
        mLastFixTimeView = (TextView) findViewById(R.id.lastFixTime);
        mLatitudeView = (TextView) findViewById(R.id.latitude);
        mLongitudeView = (TextView) findViewById(R.id.longitude);
        mTimeSinceLastFixView = (TextView) findViewById(R.id.timeSinceLastFix);
        mPrioritySpinner = (Spinner) findViewById(R.id.spinner);
        mIntervalEditText = (EditText) findViewById(R.id.interval);
        mFastestIntervalEditText = (EditText) findViewById(R.id.fastestInterval);
        mNumUpdatesEditText = (EditText) findViewById(R.id.numUpdates);
        mSmallestDisplacementEditText = (EditText) findViewById(R.id.smallestDisplacement);
        mExpirationTimeEditText = (EditText) findViewById(R.id.expirationTime);
        mExpirationDurationEditText = (EditText) findViewById(R.id.expirationDuration);
    }

    private void refreshInformationTextViews() {
        mTimeSinceLastFixView.removeCallbacks(refreshTimeSinceLastFixView);
        mLastFixTime.setToNow();
        mLastFixTimeView.setText(mLastFixTime.format("%T"));
        mLatitudeView.setText(String.valueOf(this.getLastFix().getLatitude()));
        mLongitudeView.setText(String.valueOf(this.getLastFix().getLongitude()));
        mTimeSinceLastFixView.setText(DateUtils.getRelativeTimeSpanString(mLastFixTime.normalize(true)));
        mTimeSinceLastFixView.postDelayed(refreshTimeSinceLastFixView, REFRESH_DELAY);
    }

    Runnable refreshTimeSinceLastFixView = new Runnable() {
        @Override
        public void run() {
            mTimeSinceLastFixView.setText(DateUtils.getRelativeTimeSpanString(mLastFixTime.normalize(true)));
            //remember to call removeCallbacks somewhere so this isn't eternal
            mTimeSinceLastFixView.postDelayed(this, REFRESH_DELAY);
        }
    };

    private void clearInformationTextViews() {
        mLastFixTimeView.setText("");
        mLatitudeView.setText("");
        mLongitudeView.setText("");
        mTimeSinceLastFixView.removeCallbacks(refreshTimeSinceLastFixView);
        mTimeSinceLastFixView.setText("");
    }

    private Button mStartButton;
    private Button mStopButton;
    private TextView mLastFixTimeView;
    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private TextView mTimeSinceLastFixView;
    private Spinner mPrioritySpinner;
    private EditText mIntervalEditText;
    private EditText mFastestIntervalEditText;
    private EditText mNumUpdatesEditText;
    private EditText mSmallestDisplacementEditText;
    private EditText mExpirationTimeEditText;
    private EditText mExpirationDurationEditText;
}
