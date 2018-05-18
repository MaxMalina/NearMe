package com.example.maksymg.nearme;

import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

public class NearMeActivity extends AppCompatActivity {

    private LocationManager locationManager;

    private String URL = "http://192.168.32.179:3000/";
    private Retrofit retrofit = null;


    @BindView(R.id.recycler_view) RecyclerView mUsersRecyclerView;
    private UserAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_me);
        ButterKnife.bind(this);

        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        retrofit = new Retrofit.Builder().baseUrl(URL).build();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        updateUI();
    }

    private void updateUI() {
        //TODO:GET USERS FROM SERVER

        final ArrayList<String> users = new ArrayList<>();

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
                        //TODO:CHECK DISTANCE
                        User user = User.getInstance();
                        float results[] = new float[3];
                        Location.distanceBetween(user.getLat(), user.getLon(), jsonUser.getDouble("lat"), jsonUser.getDouble("lon"), results);
                        if(!jsonUser.getString("name").equals(user.getName()) && results[0] < 100) {
                            users.add(jsonUser.getString("name"));

                            mAdapter = new UserAdapter(users);
                            mUsersRecyclerView.setAdapter(mAdapter);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class UserHolder extends RecyclerView.ViewHolder {

        public TextView mNameTextView;

        public UserHolder(View itemView) {
            super(itemView);

            mNameTextView = (TextView) itemView;
        }
    }

    private class UserAdapter extends RecyclerView.Adapter<UserHolder> {

        public List<String> mUsers;

        public UserAdapter(List<String> users) {
            mUsers = users;
        }

        @Override
        public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(NearMeActivity.this);
            View view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);

            return new UserHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserHolder holder, int position) {
            String user = mUsers.get(position);
            holder.mNameTextView.setText(user);
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }
    }
}
