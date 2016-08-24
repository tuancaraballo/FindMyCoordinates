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

    public FetchWeatherService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER_WEATHER);
        //String errorMessage = "";
        if (mReceiver == null) { //--> some error checking
            Log.wtf(TAG, "No Receiver was passed");
            return;
        }
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
            URL url = new URL(htttpURl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            buffer = new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
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
            JSONArray weather_array = root.getJSONArray("weather");
            JSONObject weather_object = weather_array.getJSONObject(0);
            JSONObject main = root.getJSONObject("main");

            String description = weather_object.getString("description");

            Double min_temp_double = (main.getDouble("temp_min") - Constants.KELVIN_CONSTANT);
            Double max_temp_double = (main.getDouble("temp_max") - Constants.KELVIN_CONSTANT);
            Double ave_temp_double = (main.getDouble("temp") - Constants.KELVIN_CONSTANT);
            int min_temp = min_temp_double.intValue();
            int max_temp = max_temp_double.intValue();
            int ave_temp = ave_temp_double.intValue();


            Log.wtf(TAG, "Description: "+ description.toString());
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

        Log.wtf(TAG,"ITS ON DELIVER_RESULT_TO_RECEIVER!!");
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.MAX_TEMP, Temp_Max);
        bundle.putInt(Constants.MIN_TEMP,Temp_Min);
        bundle.putInt(Constants.AVERAGE_TEMP,Ave_Temp);
        bundle.putString(Constants.WEATHER_DESCRIPTION, Weather_Description);
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