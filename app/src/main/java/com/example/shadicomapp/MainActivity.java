package com.example.shadicomapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.shadicomapp.adapter.CardInfoAdapter;
import com.example.shadicomapp.database.DatabaseHelper;
import com.example.shadicomapp.model.Result;
import com.example.shadicomapp.model.UserInfoModel;
import com.example.shadicomapp.network.APIInterface;
import com.example.shadicomapp.receiver.ConnectivityReceiver;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CardInfoAdapter adapter;
    private APIInterface apiInterface;
    private ArrayList<Result> dataArrayList;
    private DatabaseHelper mDbHelper;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbCreate();
        retroInstance();
        initView();
    }

    public void dbCreate() {
        int version_val = 1;
        mDbHelper = new DatabaseHelper(MainActivity.this, "Database.sqlite", null, version_val);
        mDbHelper = DatabaseHelper.getDBAdapterInstance(MainActivity.this);

        try {
            mDbHelper.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void retroInstance() {

        File httpCacheDirectory = new File(MainActivity.this.getCacheDir(), "responses");
        int cacheSize = 10 * 1024 * 1024;
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                .cache(cache).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIInterface.API_DOMAIN)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiInterface = retrofit.create(APIInterface.class);
    }

    Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {

            CacheControl.Builder cacheBuilder = new CacheControl.Builder();
            cacheBuilder.maxAge(0, TimeUnit.SECONDS);
            cacheBuilder.maxStale(365, TimeUnit.DAYS);
            CacheControl cacheControl = cacheBuilder.build();

            Request request = chain.request();
            if (ConnectivityReceiver.isConnected(getApplicationContext())) {
                request = request.newBuilder()
                        .cacheControl(cacheControl)
                        .build();
            }
            okhttp3.Response originalResponse = chain.proceed(request);
            if (ConnectivityReceiver.isConnected(getApplicationContext())) {
                int maxAge = 60 * 60; // read from cache
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
            } else {
                int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }
        }
    };


    public void initView() {
        recyclerView = findViewById(R.id.recycleview);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        if (ConnectivityReceiver.isConnected(getApplicationContext())) {
            getUserInfo();
        } else {
            progressBar.setVisibility(View.GONE);
            ArrayList<Result> results = mDbHelper.getAllData();
            adapter = new CardInfoAdapter(getApplicationContext(), results);
            recyclerView.setAdapter(adapter);
        }
    }

    private void getUserInfo() {
        dataArrayList = new ArrayList<>();
        Call<UserInfoModel> call = apiInterface.getUsers();
        call.enqueue(new Callback<UserInfoModel>() {
            @Override
            public void onResponse(Call<UserInfoModel> call, Response<UserInfoModel> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            dataArrayList = new ArrayList<>(response.body().getResults());
                            progressBar.setVisibility(View.GONE);
                            adapter = new CardInfoAdapter(getApplicationContext(), dataArrayList);
                            recyclerView.setAdapter(adapter);

                            mDbHelper.openDataBase();
                            mDbHelper.insertData(dataArrayList);
                        } else {
                            Toast.makeText(MainActivity.this, "ELSE", Toast.LENGTH_SHORT).show();
                        }

                    } catch (IllegalStateException | JsonSyntaxException exception) {
                        Log.d("CODE Catch", String.valueOf(exception));
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfoModel> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failure " + t, Toast.LENGTH_SHORT).show();
            }
        });
    }
}