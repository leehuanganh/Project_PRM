package com.example.project.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2/api/prm_api/"; // Địa chỉ API
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // Link API gốc
                    .addConverterFactory(GsonConverterFactory.create()) // Chuyển đổi JSON sang Object
                    .build();
        }
        return retrofit;
    }
}
