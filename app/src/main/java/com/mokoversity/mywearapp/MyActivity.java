package com.mokoversity.mywearapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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
import java.net.URL;


public class MyActivity extends Activity {
    private final static String TAG = "SmartWearWorkshop";


    private static IntentFilter mFilterWearableReply;
    private static BroadcastReceiver mWearableReceiver;

    private static final String ACTION_RESPONSE = "com.mokoversity.example.wearable1.REPLY";

    private MyActivity mContext;

    private static BroadcastReceiver mWeatherUpdater;
    private static IntentFilter mWeatherIntent;
    private static AsyncTask mWeatherTask;

    // REST API
    private static final String API_WEATHER = "http://api.openweathermap.org/data/2.5/weather?q=HongKong";

    //
    private static final String ACTION_WEATHER = "mokoversity.intent.action.WEATHER_UPDATE";

    @Override
         protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mContext = this;

        // Use background service
        //startService(new Intent("mokoversity.intent.action.REST"));

        // Use AsyncTask or work threader (not recommended)
        mWeatherIntent = new IntentFilter(ACTION_WEATHER);
        mWeatherUpdater = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                mWeatherTask = new WeatherTask();
                mWeatherTask.execute();
            }
        };

        new WeatherTask().execute();
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(mWeatherUpdater, mWeatherIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mWeatherUpdater);
    }

    private void zero() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Zero")
                .setContentText("You're at step zero.")
                .setSmallIcon(R.drawable.bg_eliza);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0x07, builder.build());
    }

    // set background on wearbale device
    private void one() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Onw")
                .setContentText("You're at step one.")
                .setSmallIcon(R.drawable.bg_eliza)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.bg_eliza));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0x08, builder.build());
    }


    // Add second page
    private void two() {
        // main notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Two")
                .setContentText("You're at step two.")
                .setSmallIcon(R.drawable.bg_eliza)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.bg_eliza));

        // second page
        NotificationCompat.Builder secondPageNotification = new NotificationCompat.Builder(this)
                .setContentTitle("Second Page")
                .setContentText("This is second page");

        // Add second page with wearable extender and extend the main notification
        Notification notification = new WearableExtender()
                .addPage(secondPageNotification.build())
                .extend(builder)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0x09, notification);
    }

    // Add action
    private void three() {
        // Build an intent for an action
        Intent actionIntent = new Intent(Intent.ACTION_VIEW);
        PendingIntent actionPendingIntent = PendingIntent.getActivity(this, 0, actionIntent, 0);

        // Notification with action
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Three")
                .setContentText("You're at step three.")
                .setSmallIcon(R.drawable.bg_eliza)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.bg_eliza))
                .addAction(R.drawable.ic_full_reply, "Click me", actionPendingIntent);

        Notification notification = new WearableExtender()
                .extend(builder)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0x01, notification);
    }


    private void four() {
        // Build an intent for an action to make a phone call
        Intent phoneCallIntent = new Intent(Intent.ACTION_VIEW);
        Uri phoneUri = Uri.parse("tel:119");
        phoneCallIntent.setData(phoneUri);
        PendingIntent phoneCallPendingIntent = PendingIntent.getActivity(this, 0, phoneCallIntent, 0);

        // Notification with action
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Four")
                .setContentText("You're at step four.")
                .setSmallIcon(R.drawable.bg_eliza)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.bg_eliza))
                .addAction(R.drawable.ic_full_reply, "Call me", phoneCallPendingIntent);

        Notification notification = new WearableExtender()
                .extend(builder)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0x01, notification);
    }


    private void coupon() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Coupon")
                .setContentText("Limited. 30 mins from now !")
                .setSmallIcon(R.drawable.bg_eliza);

        Intent intent = new Intent(ACTION_RESPONSE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0x07, builder.build());
    }

    private void choice() {
        // Create an intent for the reply action
        Intent replyIntent = new Intent(this, MyActivity.class);
        PendingIntent replyPendingIntent =
                PendingIntent.getActivity(this, 0, replyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        String[] choices = { "Yes", "No" };

        RemoteInput remoteInput = new RemoteInput.Builder("reply")
                .setLabel("Reply")
                .setChoices(choices)
                .build();

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.ic_reply_icon,
                        "Say Yes or No", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("YES&NO")
                .setContentText(":-)")
                .setSmallIcon(R.drawable.bg_eliza)
                .extend(new WearableExtender().addAction(action));


        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0x07, builder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //
    // Supporting APIs
    //

    private class WeatherTask extends AsyncTask<Object, Integer, Long> {

        @Override
        protected Long doInBackground(Object... urls) {
            long totalSize = 0;

            invokeOpenWeatherMapAPI(API_WEATHER);

            return totalSize;
        }
    }

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
                    //Log.i(TAG, "Line: " + line);
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

    private void notifyPrice(int price) {
        String message = Integer.toString(price);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Live Gold Price")
                .setContentText(message)
                .setSmallIcon(R.drawable.bg_eliza)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.bg_eliza));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0x08, builder.build());
    }

    private void notifyHumidity(int humidity, double temp) {
        String message = "Temp F: " + Double.toString(temp);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Weather HongKong")
                .setContentText(message)
                .setSmallIcon(R.drawable.bg_eliza)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.bg_eliza));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0x08, builder.build());
    }

    private void notifyHumidityWithAction(int humidity, double temp) {
        String message = "Temp F: " + Double.toString(temp);

        Intent intent = new Intent(ACTION_WEATHER);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);

        // Notification with action
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Weather HongKong")
                .setContentText(message)
                .setSmallIcon(R.drawable.bg_eliza)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.bg_eliza))
                .addAction(R.drawable.ic_full_reply, "Refresh", pendingIntent);

        Notification notification = new WearableExtender()
                .extend(builder)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0x01, notification);
    }

    private void invokeRestAPI(String mJsonUrl) {
        /*
        {
          "success": true,
          "errors": [],
          "errfor": {},
          "data": {
            "price": "1311",
            "move": "-9.38",
            "timestamp": "2014-07-19T07:36:10.978Z"
          }
        }
        */
        String jsonStr = retrieveJSONCache(mJsonUrl);

        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONObject json2 = json.getJSONObject("data");
            int price = json2.getInt("price");

            notifyPrice(price);
        } catch (JSONException e) {

        }
    }

    private void invokeOpenWeatherMapAPI(String mJsonUrl) {
        /*
        {
          "success": true,
          "errors": [],
          "errfor": {},
          "data": {
            "price": "1311",
            "move": "-9.38",
            "timestamp": "2014-07-19T07:36:10.978Z"
          }
        }
        */
        String jsonStr = retrieveJSONCache(mJsonUrl);

        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONObject json2 = json.getJSONObject("main");
            int humidity = json2.getInt("humidity");
            double temp = json2.getDouble("temp");

            ///notifyHumidity(humidity, temp);
            notifyHumidityWithAction(humidity, temp);
        } catch (JSONException e) {

        }
    }
}
