package com.tuananhcaraballo.findmycoordinates;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Antuan on 8/23/16.
 */
public class FetchWeatherService extends IntentService{


    protected ResultReceiver mReceiver; // --> receiver sent by the user as an extra in the intent

    private static final String TAG = "FetchWeatherService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchWeatherService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
      //  mReceiver = intent.getParcelableExtra(Constants.RECEIVER_WEATHER);
        String errorMessage = "";
//        if (mReceiver == null) { //--> some error checking
//            Log.wtf(TAG, "No Receiver was passed");
//            return;
//        }
        Log.wtf(TAG, "GOT TO THE FETCHWEATHER SERVICE!");


         Double Longitude = Double.parseDouble(intent.getStringExtra(Constants.LONGITUDE));
         Double Latitude =  Double.parseDouble(intent.getStringExtra(Constants.LATITUDE));

        Log.wtf(TAG, "GOT THE latitude");
        Log.wtf(TAG, "LATITUDE: "+Latitude.toString()+ "  LONGITUDE: "+Longitude.toString());

        if (Latitude == null || Longitude == null) { // --> some error checking
            errorMessage = "No Longitude or Latitude was provided";
            Log.wtf(TAG, errorMessage);
            //deliverResultToReceiver(Constants.FAILURE_RESULT,errorMessage);
            return;
        }

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuffer buffer;

        //Udacity
        String foreCastJson = null;

        String htttpURl = "http://api.openweathermap.org/data/2.5/weather?lat="+ Latitude.toString() +"&lon=" +
                Longitude.toString() +"&APPID=e961668dcf266409643ffcca78dacaa9";
        Log.wtf(TAG, htttpURl);





        try {

            Log.wtf(TAG, "GOT TO THE TRY AND CATCH! -- 1");
            URL url = new URL(htttpURl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            Log.wtf(TAG, "GOT TO THE TRY AND CATCH! -- 2");
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            buffer = new StringBuffer();
            Log.wtf(TAG, "GOT TO THE TRY AND CATCH! -- 3");
            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            Log.wtf(TAG, "GOT TO THE TRY AND CATCH! -- 4");
            parseString(buffer);


        } catch (MalformedURLException e) {
            Log.wtf(TAG, "MaLFORMED INPUT");
            e.printStackTrace();
        } catch (IOException e) {
            Log.wtf(TAG, "IOException e -- 1");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                Log.wtf(TAG, "CONNECTION NOT NULL");
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    Log.wtf(TAG, "CLOSING READER");
                    reader.close();
                }
            } catch (IOException e) {
                Log.wtf(TAG, "IOException e -- 2");
                e.printStackTrace();
            }
        }
    }

    private void parseString(StringBuffer buffer){
        String finalJson = buffer.toString();
        Log.wtf(TAG, "GOT TO PARSE STRING" + finalJson);

        try {
            JSONObject root = new JSONObject(finalJson);
            Log.wtf(TAG, "IT SEEMS THAT PARSING HAS BEEN SUCCESSFUL!!");
             // Log.v(TAG, "-- IT SEEMS THAT PARSING HAS BEEN SUCCESSFUL!");
            JSONArray weather_array = root.getJSONArray("weather");
            JSONObject weather_object = weather_array.getJSONObject(0);
            JSONObject main = root.getJSONObject("main");

            String description = weather_object.getString("description");
            int min_temp = Integer.parseInt(main.getString("temp_min")) - Constants.KELVIN_CONSTANT;
            int max_temp = Integer.parseInt(main.getString("temp_max")) - Constants.KELVIN_CONSTANT;
            int ave_temp = Integer.parseInt(main.getString("temp")) - Constants.KELVIN_CONSTANT;

            Log.wtf(TAG, "Description: "+ description);
            Log.wtf(TAG, "Min_Temp: "+ min_temp);
            Log.wtf(TAG, "Max_Temp: "+ max_temp);
            Log.wtf(TAG, "Ave_Temp: "+ ave_temp);

            deliverResultToReceiver(Constants.SUCCESS_RESULT,min_temp,max_temp,ave_temp,description);


        } catch (JSONException e) {
            Log.wtf(TAG, " PARSING WAS  UNSUCCESSFUL!!");
            e.printStackTrace();
        }

    }

  // add also the icon, you need to get it into a different http Connection
    private void deliverResultToReceiver(int resultCode, int Temp_Min, int Temp_Max, int Ave_Temp, String Weather_Description) {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.MAX_TEMP, Temp_Max);
        bundle.putInt(Constants.MIN_TEMP,Temp_Min);
        bundle.putString(Constants.WEATHER_DESCRIPTION, Weather_Description);

        Log.v(TAG,"ITS ON DELIVER_RESULT_TO_RECEIVER!!");
        mReceiver.send(resultCode,bundle);
    }

}
/*

     //int Latitude = -1;
        //int Longitude = -1;
       // Double Lat = Double.parseDouble(intent.getStringExtra(Constants.LATITUDE));
        //Double Long = Double.parseDouble(intent.getStringExtra(Constants.LONGITUDE));

        //Latitude = Lat.intValue();
        //Longitude = Long.intValue();


        //Latitude = Integer.valueOf(intent.getStringExtra(Constants.LATITUDE));


 */