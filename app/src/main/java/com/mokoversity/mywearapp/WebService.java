package com.mokoversity.mywearapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WebService extends Service {
    private final static String TAG = "SmartWearWorkshop";
    private final static String sAPIUrl = "http://priceover.net/1/price/gold";

    private DownloadThread mThread;

    class DownloadThread extends Thread {

        WebService mContext;

        DownloadThread(Context ctx) {
            // aggregate, down casting
            mContext = (WebService)ctx;
        }

        @Override
        public void run() {
            mContext.invokeRestAPI(sAPIUrl);
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "onStart");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        mThread = new DownloadThread(this);

        return 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //
    // Supporting APIs
    //

    private static JSONArray mJsonArray = null;

    private String retrieveJSONCache(String json_url) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(json_url);

        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(TAG, "Failed to download file");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    public void invokeRestAPI(String mJsonUrl) {
        // Download JSON cache file
        String JSONCache = retrieveJSONCache(mJsonUrl);

        try {
            mJsonArray = new JSONArray(JSONCache);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < mJsonArray.length(); i++) {
            try {
                JSONObject jsonObject = mJsonArray.getJSONObject(i);
                String url = jsonObject.getString("item");

                Log.i(TAG, "url: " + url);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
