package com.example.project.api;

import com.example.project.response.LoginResponse;
import com.example.project.response.RegisterResponse;
import com.example.project.response.TransactionResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    // Đăng ký người dùng
    @FormUrlEncoded
    @POST("user_api.php")
    Call<RegisterResponse> registerUser(
            @Field("action") String action,
            @Field("email") String email,
            @Field("password") String password
    );

    // Đăng nhập người dùng
    @FormUrlEncoded
    @POST("user_api.php")
    Call<LoginResponse> loginUser(
            @Field("action") String action,
            @Field("email") String email,
            @Field("password") String password
    );

    // Thêm giao dịch mới
    @FormUrlEncoded
    @POST("transaction_api.php")
    Call<TransactionResponse> addTransaction(
            @Field("action") String action,
            @Field("user_id") int userId,
            @Field("category") String category,
            @Field("amount") double amount,
            @Field("date") String date,
            @Field("note") String note,
            @Field("type") String type
    );

    // Cập nhật giao dịch
    @FormUrlEncoded
    @POST("transaction_api.php")
    Call<TransactionResponse> updateTransaction(
            @Field("action") String action,
            @Field("transaction_id") int transactionId,
            @Field("category") String category,
            @Field("amount") double amount,
            @Field("date") String date,
            @Field("note") String note,
            @Field("type") String type
    );

    // Xóa giao dịch
    @FormUrlEncoded
    @POST("transaction_api.php")
    Call<TransactionResponse> deleteTransaction(
            @Field("action") String action,
            @Field("transaction_id") int transactionId
    );

}
