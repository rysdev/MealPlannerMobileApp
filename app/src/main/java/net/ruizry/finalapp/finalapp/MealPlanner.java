package net.ruizry.finalapp.finalapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.EditText;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MealPlanner extends AppCompatActivity {

    private AuthorizationService mAuthorizationService;
    private AuthState mAuthState;
    private OkHttpClient mOkHttpClient;

    //private static final String TAG = MealPlanner.class.getSimpleName();
    //private TextView userResponse;
    String gUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences authPreference = getSharedPreferences("auth", MODE_PRIVATE);
        setContentView(R.layout.activity_meal_planner);
        mAuthorizationService = new AuthorizationService(this);
        ((Button)findViewById(R.id.view_meals)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if(e == null){
                                mOkHttpClient = new OkHttpClient();
                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plus/v1/people/me");
                                //reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyD6J8UTiI__L3PvEAsx__exqNP9pCKZF-s").build();
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .addHeader("Authorization", "Bearer " + accessToken)
                                        .build();
                                //userResponse = (TextView) findViewById(R.id.user_response);
                                //userResponse.setText(accessToken);
                                //Log.d(TAG, "token: " + accessToken);
                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r = response.body().string();
                                        try {
                                            JSONObject j = new JSONObject(r);
                                            gUser = j.getString("id");
                                            //Log.d(TAG, "object: " + gUser);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //userResponse.setText(gUser);
                                                }
                                            });
                                            //Log.d(TAG, "After ui update: " + gUser);

                                            HttpUrl reqUrl = HttpUrl.parse("https://ruizryhelloworld.appspot.com/seemeal");
                                            //reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyD6J8UTiI__L3PvEAsx__exqNP9pCKZF-s").build();
                                            String json = "{\"user\":\"" + gUser + "\"}";
                                            final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                                            RequestBody body = RequestBody.create(mediaType, json);
                                            Request request = new Request.Builder()
                                                    .url(reqUrl)
                                                    .post(body)
                                                    .build();
                                            mOkHttpClient.newCall(request).enqueue(new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    e.printStackTrace();
                                                }

                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    String r = response.body().string().replace("'", "\"");
                                                    try {
                                                        //JSONObject j = new JSONObject(r);
                                                        JSONArray items = new JSONArray(r);
                                                        //JSONObject allboats = j.getJSONObject("Boat");
                                                        //JSONArray boats = allboats.getJSONArray("Key");
                                                        //Log.d(TAG, "Meal response: " + r);
                                                        List<Map<String,String>> posts = new ArrayList<Map<String,String>>();
                                                        for(int i = 0; i < items.length(); i++){
                                                            HashMap<String, String> m = new HashMap<String, String>();
                                                            m.put("food", items.getJSONObject(i).getString("food"));
                                                            m.put("date",items.getJSONObject(i).getString("date"));
                                                            m.put("who_cooks",items.getJSONObject(i).getString("who_cooks"));
                                                            m.put("which_meal",items.getJSONObject(i).getString("which_meal"));
                                                            posts.add(m);
                                                        }
                                                        final SimpleAdapter postAdapter = new SimpleAdapter(
                                                                MealPlanner.this,
                                                                posts,
                                                                R.layout.meal_list,
                                                                new String[]{"food", "date", "who_cooks", "which_meal"},
                                                                new int[]{R.id.food_item, R.id.date_item, R.id.who_item, R.id.which_item});
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                ((ListView)findViewById(R.id.meal_list_display)).setAdapter(postAdapter);
                                                            }
                                                        });
                                                    } catch (JSONException e2) {
                                                        e2.printStackTrace();
                                                    }

                                                }
                                            });
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }

                                    }
                                });

                            }
                        }
                    });
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        ((Button)findViewById(R.id.addmeal_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
                        @Override
                        public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException e) {
                            if(e == null){
                                mOkHttpClient = new OkHttpClient();
                                HttpUrl reqUrl = HttpUrl.parse("https://www.googleapis.com/plus/v1/people/me");
                                //reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyD6J8UTiI__L3PvEAsx__exqNP9pCKZF-s").build();
                                Request request = new Request.Builder()
                                        .url(reqUrl)
                                        .addHeader("Authorization", "Bearer " + accessToken)
                                        .build();
                                //userResponse = (TextView) findViewById(R.id.user_response);
                                //userResponse.setText(accessToken);
                                //Log.d(TAG, "token: " + accessToken);
                                mOkHttpClient.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String r = response.body().string();
                                        try {
                                            JSONObject j = new JSONObject(r);
                                            gUser = j.getString("id");
                                            //Log.d(TAG, "object: " + gUser);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //userResponse.setText(gUser);
                                                }
                                            });
                                            //Log.d(TAG, "After ui update: " + gUser);

                                            HttpUrl reqUrl = HttpUrl.parse("https://ruizryhelloworld.appspot.com/addmeal");
                                            //reqUrl = reqUrl.newBuilder().addQueryParameter("key", "AIzaSyD6J8UTiI__L3PvEAsx__exqNP9pCKZF-s").build();
                                            String json = "{\"user\":\"" + gUser + "\", \"food\":\"" + ((EditText) findViewById(R.id.edit_food)).getText().toString() + "\", \"date\":\"" + ((EditText) findViewById(R.id.edit_date)).getText().toString() + "\", \"who_cooks\":\"" + ((EditText) findViewById(R.id.edit_who)).getText().toString() + "\", \"which_meal\":\"" + ((EditText) findViewById(R.id.edit_which)).getText().toString() + "\"}";
                                            final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                                            RequestBody body = RequestBody.create(mediaType, json);
                                            Request request = new Request.Builder()
                                                    .url(reqUrl)
                                                    .post(body)
                                                    .build();
                                            mOkHttpClient.newCall(request).enqueue(new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    e.printStackTrace();
                                                }

                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    String r = response.body().string().replace("'", "\"");

                                                }


                                            });
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }

                                    }
                                });

                            }
                        }
                    });
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onStart(){
        mAuthState = getOrCreateAuthState();
        super.onStart();

    }

    AuthState getOrCreateAuthState(){
        AuthState auth = null;
        SharedPreferences authPreference = getSharedPreferences("auth", MODE_PRIVATE);
        String stateJson = authPreference.getString("stateJson", null);
        if(stateJson != null){
            try {
                auth = AuthState.jsonDeserialize(stateJson);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        if( auth != null && auth.getAccessToken() != null){
            return auth;
        } else {
            updateAuthState();
            return null;
        }
    }

    void updateAuthState(){

        Uri authEndpoint = new Uri.Builder().scheme("https").authority("accounts.google.com").path("/o/oauth2/v2/auth").build();
        Uri tokenEndpoint = new Uri.Builder().scheme("https").authority("www.googleapis.com").path("/oauth2/v4/token").build();
        Uri redirect = new Uri.Builder().scheme("net.ruizry.finalapp.finalapp").path("foo").build();

        AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(authEndpoint, tokenEndpoint, null);
        AuthorizationRequest req = new AuthorizationRequest.Builder(config, "210517853129-48d0hmnb5rjrr3flenvcqglds7k0v4lg.apps.googleusercontent.com", ResponseTypeValues.CODE, redirect)
                .setScopes("https://www.googleapis.com/auth/plus.me", "https://www.googleapis.com/auth/plus.stream.write", "https://www.googleapis.com/auth/plus.stream.read", "https://www.googleapis.com/auth/plus.profile.emails.read")
                .build();

        Intent authComplete = new Intent(this, AuthCompleteActivity.class);
        mAuthorizationService.performAuthorizationRequest(req, PendingIntent.getActivity(this, req.hashCode(), authComplete, 0));
    }
}
