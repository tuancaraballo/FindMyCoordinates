package com.tuananhcaraballo.findmycoordinates;

import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;

/**
 * Created by Antuan on 7/25/16.
 */
public class FetchAddressIntentService extends IntentService{


    private static final String TAG = "FetchAddressIntentService";
    protected ResultReceiver mReceiver; // --> receiver sent by the user as an extra in the intent

    public FetchAddressIntentService() { //-->Constructor
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";
        mReceiver
    }
}
