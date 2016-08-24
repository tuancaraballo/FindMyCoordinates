package com.tuananhcaraballo.findmycoordinates;

/**
 * Created by Antuan on 7/25/16.
 * Constant values used across the program
 * Notice how they are static, meaning that you don't have the instantiate an
 * object of type Constants :-)
 */
public class Constants {
    public static final int SUCCESS_RESULT = 0; // google reference has it as 0
    public static final int FAILURE_RESULT = 1;
    public  static  final String PACKAGE_NAME = "com.tuananhcaraballo.findmycoordinates";
    public static  final  String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static  final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY ";
    public  static  final  String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String RECEIVER_WEATHER = PACKAGE_NAME + ".RECEIVER_WEATHER";
    public static final String MAX_TEMP = "MAXIMUM_TEMPERATURE";
    public static final String MIN_TEMP = "MINIMUM_TEMPERATURE";
    public static final String AVERAGE_TEMP = "MINIMUM_TEMPERATURE";
    public static final String WEATHER_DESCRIPTION = "WEATHER_DESCRIPTION";
    public static final Double KELVIN_CONSTANT = 273.15;


}
