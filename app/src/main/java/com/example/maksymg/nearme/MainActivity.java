package com.example.maksymg.nearme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tvEnabledGPS) TextView tvEnabledGPS;
    @BindView(R.id.tvStatusGPS) TextView tvStatusGPS;
    @BindView(R.id.tvLocationGPS) TextView tvLocationGPS;
    @BindView(R.id.tvEnabledNet) TextView tvEnabledNet;
    @BindView(R.id.tvStatusNet) TextView tvStatusNet;
    @BindView(R.id.tvLocationNet) TextView tvLocationNet;

    private LocationManager locationManager;

    private String URL = "http://192.168.32.179:3000/";
    private Retrofit retrofit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        retrofit = new Retrofit.Builder().baseUrl(URL).build();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                locationListener);
        checkEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }

        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling

                return;
            }
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                tvStatusGPS.setText("Status: " + String.valueOf(status));
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                tvStatusNet.setText("Status: " + String.valueOf(status));
            }
        }
    };

    class SendingUserTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            removeUsersWithSameName();
            return strings[0];
        }

        @Override
        protected void onPostExecute(String result) {
            postRequest(result);
        }
    }

    private void SendLocation(String data) {
        new SendingUserTask().execute(data);
    }

    private void removeUsersWithSameName() {
        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .url(URL + "users")
                .build();

        retrofit.callFactory().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("GET", "Error sending data Retrofit");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("GET", "OK");
                try {
                    JSONArray jsonUsers = new JSONArray(response.body().string());
                    System.out.println(jsonUsers);
                    for(int i = 0; i < jsonUsers.length(); i++) {
                        JSONObject jsonUser = jsonUsers.getJSONObject(i);
                        if(jsonUser.getString("name").equals(User.getInstance().getName())) {
                            deleteRequest(jsonUser);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void deleteRequest(JSONObject jsonUser) throws JSONException {
        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .url(URL + "users/" + jsonUser.getInt("id"))
                .delete()
                .build();

        retrofit.callFactory().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("DELETE", "Error sending data Retrofit");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("DELETE", "OK");
            }
        });
    }

    private void postRequest(String data) {
        RequestBody jsonBody = RequestBody.create(MediaType.parse("application/json"), data);

        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .url(URL + "users")
                .post(jsonBody)
                .build();

        retrofit.callFactory().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("POST", "Error sending data Retrofit");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("POST", "OK");
            }
        });
    }

    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            tvLocationGPS.setText(formatLocation(location));
            SendUser(location);
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            tvLocationNet.setText(formatLocation(location));
            SendUser(location);
        }
    }

    private void SendUser(Location location) {
        User user = User.getInstance();
        user.setLat(location.getLatitude());
        user.setLon(location.getLongitude());

        Gson jsonUser = new Gson();
        SendLocation(jsonUser.toJson(user));
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }

    private void checkEnabled() {
        tvEnabledGPS.setText("Enabled: "
                + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        tvEnabledNet.setText("Enabled: "
                + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    public void onClickFindNearest(View view) {
        //TODO:FIND NEAREST, MAYBE NEW SCREEN
        Intent intent = new Intent(MainActivity.this, NearMeActivity.class);
        startActivity(intent);
    }

}

