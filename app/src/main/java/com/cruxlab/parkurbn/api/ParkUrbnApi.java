package com.cruxlab.parkurbn.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.cruxlab.parkurbn.ParkUrbnApplication;
import com.cruxlab.parkurbn.model.Segment;
import com.cruxlab.parkurbn.model.Spot;
import com.cruxlab.parkurbn.model.Vehicle;
import com.cruxlab.parkurbn.model.request.ChangePasswordBody;
import com.cruxlab.parkurbn.model.request.CreateVehicleBody;
import com.cruxlab.parkurbn.model.request.DeleteVehicleBody;
import com.cruxlab.parkurbn.model.request.FeedbackBody;
import com.cruxlab.parkurbn.model.request.LoginBody;
import com.cruxlab.parkurbn.model.request.LoginFBBody;
import com.cruxlab.parkurbn.model.request.MapRequest;
import com.cruxlab.parkurbn.model.request.PaymentRequest;
import com.cruxlab.parkurbn.model.request.SpotsRequest;
import com.cruxlab.parkurbn.model.response.HistoryResponse;
import com.cruxlab.parkurbn.model.response.PaymentResponse;
import com.cruxlab.parkurbn.model.response.TokenResponse;
import com.cruxlab.parkurbn.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

public interface ParkUrbnApi {

    String APP_NAME = "ParkUrbn";
    String PREF_COOKIES = "cookies";

    String HOST_NAME = "dev.parkurbn.cruxlab.io";

    @POST("/spots")
    Call<List<Spot>> getSpots(@Body MapRequest mapRequest);

    @POST("/segments")
    Call<List<Segment>> getSegments(@Body MapRequest mapRequest);

    @POST("/arrived")
    Call<List<Spot>> arrived(@Body SpotsRequest spotsRequest);

    @POST("/best_distance_spots")
    Call<List<Spot>> getBestDistanceSpots(@Body SpotsRequest spotsRequest);

    @GET("/pay")
    Call<TokenResponse> getClientToken();

    @POST("/pay")
    Call<PaymentResponse> pay(@Body PaymentRequest paymentRequest);

    @POST("/registration")
    Call<JsonObject> registerUser(@Body LoginBody loginBody);

    @POST("/login")
    Call<JsonObject> loginUser(@Body LoginBody body);

    @POST("/login_fb")
    Call<JsonObject> loginFBUser(@Body LoginFBBody body);

    @POST("/logout")
    Call<JSONObject> logout();

    @POST("/forgot_pass")
    Call<JSONObject> forgotPassword(@Body LoginBody body);

    @GET("/change_user")
    Call<User> getUser();

    @PATCH("/change_user")
    Call<User> modifyUser(@Body User body);

    @PATCH("/change_user")
    Call<User> changePass(@Body ChangePasswordBody body);

    @GET("/vehicle")
    Call<List<Vehicle>> getVehicleList();

    @POST("/vehicle")
    Call<Vehicle> createVehicle(@Body CreateVehicleBody body);

    @HTTP(method = "DELETE", path = "/vehicle", hasBody = true)
    Call<JsonObject> deleteVehicle(@Body DeleteVehicleBody body);

    @PATCH("/vehicle")
    Call<Vehicle> changeVehicle(@Body Vehicle vehicle);

    @GET("/payment_history")
    Call<HistoryResponse> getParkingHistory();

    @POST("/feedback")
    Call<JSONObject> sendFeedback(@Body FeedbackBody body);

    class Api {

        private static Retrofit retrofit;

        public static ParkUrbnApi create() {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .addInterceptor(new AddCookiesInterceptor())
                    .addInterceptor(new ReceivedCookiesInterceptor())
                    .addInterceptor(new ConnectivityInterceptor(ParkUrbnApplication.get()))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS);

            OkHttpClient client = builder.build();

            Gson gson = new GsonBuilder().setLenient().create();
            retrofit = new Retrofit.Builder()
                    .baseUrl("http://" + HOST_NAME)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            return retrofit.create(ParkUrbnApi.class);
        }
    }

    /**
     * This interceptor puts all the Cookies in Preferences in the Request.
     */
    class AddCookiesInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request.Builder builder = chain.request().newBuilder();
            HashSet<String> preferences = (HashSet<String>) ParkUrbnApplication.get().getSharedPreferences(APP_NAME, Context.MODE_PRIVATE).getStringSet(PREF_COOKIES, new HashSet<String>());
            for (String cookie : preferences) {
                builder.addHeader("Cookie", cookie);
            }

            return chain.proceed(builder.build());
        }
    }

    /**
     * This Interceptor adds all received Cookies to the app DefaultPreferences.
     */
    class ReceivedCookiesInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());

            if (!originalResponse.headers("Set-Cookie").isEmpty()) {
                HashSet<String> cookies = new HashSet<>();

                for (String header : originalResponse.headers("Set-Cookie")) {
                    cookies.add(header);
                }

                ParkUrbnApplication.get().getSharedPreferences(APP_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putStringSet(PREF_COOKIES, cookies)
                        .apply();
            }

            return originalResponse;
        }
    }

    class ConnectivityInterceptor implements Interceptor {

        private Context mContext;

        public ConnectivityInterceptor(Context context) {
            mContext = context;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            boolean connected = netInfo != null && netInfo.isConnected();

            if (!connected) {
                throw new NoConnectivityException();
            }

            Request.Builder builder = chain.request().newBuilder();
            return chain.proceed(builder.build());
        }
    }
}
