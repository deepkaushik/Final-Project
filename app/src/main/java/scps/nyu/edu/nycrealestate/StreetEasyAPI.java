package scps.nyu.edu.nycrealestate;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// this class queries a StreetEasy API to get aggregate real estate statictics for a zipcode
// and number of bedrooms and then saves that data to our SQL database for future reference
// (because the StreetEasy API only allows 100 queries per hour so we need to save the data locally)

public class StreetEasyAPI {

    private Context context;
    private String zipCode;
    private String nbrBedrooms;

    public void updSQLDatabase(String zipCode, String nbrBedrooms, Context context) {
        this.context = context;
        this.zipCode = zipCode;
        this.nbrBedrooms = nbrBedrooms;

        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(zipCode, nbrBedrooms);
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {
            //Must be declared outside of try block,
            //so we can mention them in finally block.
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;

            String zipCode = args[0];
            String numberBedrooms = args[1];
            String apiKey = "979abdd55a88997fe6e48f0754afbb1084c652dd";

            //http://streeteasy.com/nyc/api/sales/data?criteria=zip:11102|beds:3&key=979abdd55a88997fe6e48f0754afbb1084c652dd&format=json

            try {
                String urlString = "http://streeteasy.com/nyc/api/sales/data"
                        + "?criteria=zip:" + zipCode
                        + "|beds:" + numberBedrooms  // number of beds in apartment
                        + "&key=" + apiKey
                        + "&format=json";     //vs. xml or html
                URL url = new URL(urlString);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                if (inputStream == null) {
                    return null;
                }

                StringBuilder stringBuilder = new StringBuilder();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                if (stringBuilder.length() == 0) {
                    return null;          //Pass null to onPostExecute.
                }
                return stringBuilder.toString(); //Pass String to onPostExecute.
            } catch (IOException exception) {
                Log.e("myTag", "doInBackground:", exception);
                return null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException exception) {
                        Log.e("myTag", "doInBackground:", exception);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String json) { //json is the String returned by doInBackground
            useTheResult(json);
        }
    }

    private void useTheResult(String json) {

        try {
            JSONObject jSONObject = new JSONObject(json);
            Double avgPrice = jSONObject.getDouble("average_price");
            Double avgSqFt = jSONObject.getDouble("average_sqft");
            Double avgWom = jSONObject.getDouble("average_wom");

            SQLHelper helper = new SQLHelper(context, "streeteasy_stats.db");

            if (avgPrice != null) {
                // save data to SQL database so we can reference it later
                // (since our StreetEast API only allows 100 queries per hour)

                SQLiteDatabase database = helper.getWritableDatabase();

                // delete all previous shapes before saving
                //database.execSQL("DELETE FROM " + helper.getTableName());

                    ContentValues contentValues = new ContentValues();
                    contentValues.put("ZIPCODE", zipCode);
                    contentValues.put("NBRBEDROOMS", nbrBedrooms);
                    contentValues.put("AVGPRICE", avgPrice);
                    contentValues.put("AVGSQFT", avgSqFt);
                    contentValues.put("AVGWOM", avgWom);

                    database.insert(helper.getTableName(), null, contentValues);
            }

            //{"updated_at":"2015-08-13T23:56:14-04:00",
            // "listing_count":3,
            // "median_price":1025000,
            // "average_price":1754666,
            // "stddev_price":1518252.3944764037,
            // "percentile_10_price":739000,
            // "percentile_90_price":3500000,
            // "sqft_count":2,
            // "median_sqft":1873,
            // "average_sqft":1873,
            // "stddev_sqft":745.2905473706211,
            // "median_ppsf":1003,
            // "average_ppsf":1131,
            // "increase_count":0,
            // "average_increase":0.0,
            // "stddev_increase":0.0,
            // "decrease_count":2,
            // "average_decrease":819.0,
            // "stddev_decrease":97.58073580374356,
            // "wom_count":2,
            // "median_wom":19.0,
            // "average_wom":19.5,
            // "stddev_wom":6.363961030678928,
            // "criteria":"zip:11102|beds:3",
            // "criteria_description":"Sale listings in zip code  11102  with 3 bedrooms  ",
            // "search_url":"http://streeteasy.com/for-sale/nyc/zip:11102%7Cbeds:3"}


        } catch (JSONException exception) {
            //
        }
    }

}
