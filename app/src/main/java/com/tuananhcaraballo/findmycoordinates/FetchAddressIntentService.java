package com.tuananhcaraballo.findmycoordinates;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Antuan on 7/25/16.
 */
public class FetchAddressIntentService extends IntentService{


    private static final String TAG = "FetchAddressIS";
    protected ResultReceiver mReceiver; // --> receiver sent by the user as an extra in the intent

    public FetchAddressIntentService() { //-->Constructor
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";
        // 1 --> Gets receiver from intent that was passed from MainActivity as an extra
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        if(mReceiver == null){ //--> some error checking
            Log.wtf(TAG,"No Receiver was passed");
            return;
        }
        // 2 --> Location that was passed to this activity from MainActivity as an extra
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        if(location == null){ // --> some error checking
            errorMessage = "No location was provided";
            Log.wtf(TAG,errorMessage);
            deliverResultToReceiver(Constants.FAILURE_RESULT,errorMessage);
            return;
        }
        // 3 --> Define a geocoder Object
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        // 4 --> Declare a List of type Addresses:
        List<Address> addresses = null;

        // 5 --> Get the address:
        try{
            addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(),1);
        }catch (IOException ioExcpetion){ //-->catch network or other I/O problems
            errorMessage = "Service not available";
            Log.e(TAG,errorMessage,ioExcpetion);
        }catch (IllegalArgumentException illegalArgumentException){
            //-->catches invalid latitude or longitude values
            errorMessage = "Invalid latitude or longitude";
            Log.e(TAG,errorMessage,illegalArgumentException);
        }

        // 6 --> Check if address was not found
        if(addresses == null || addresses.size() ==0 ){
            if(errorMessage.isEmpty()){
                errorMessage == "Sorry, no address was found";
                Log.e(TAG,errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT,errorMessage);
        }else{  //--> the address was found
            Address address = addresses.get(0);
            ArrayList<String> addressElements = new ArrayList<~>();

            // --> iterate through each element in the addrs object and append it
            // to the ArrayList
            for (int i = 0; i<address.getMaxAddressLineIndex(); i++){
                addressElements.add(address.getAddressLine(i));
            }
            Log.i(TAG,"Address was found");

            // -->Check out how cool the TxtUtils function is, read more about it
            // later
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressElements));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode,bundle);
    }
}
