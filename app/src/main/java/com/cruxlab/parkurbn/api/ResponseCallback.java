package com.cruxlab.parkurbn.api;

import android.util.Log;
import android.widget.Toast;

import com.cruxlab.parkurbn.ParkUrbnApplication;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.model.response.ErrorResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Base class for response callbacks. It checks whether request was successful and continue
 * handle response only if it is so. Otherwise error message may be displayed.
 * <p>
 * Created by alla on 4/12/17.
 */

public abstract class ResponseCallback<T> implements Callback<T> {

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            handleResponse(response);
        } else {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            try {
                ErrorResponse error = gson.fromJson(response.errorBody().string(), ErrorResponse.class);
                showErrorMessage(error);
            } catch (JsonSyntaxException | IOException e) {
                onFailure(call, e);
            }
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        t.printStackTrace();
        if (t instanceof NoConnectivityException) {
            showNetworkAlert();
        }
    }

    abstract public void handleResponse(Response<T> response);

    protected void showErrorMessage(ErrorResponse response) {

    }

    private void showNetworkAlert() {
        Toast.makeText(ParkUrbnApplication.get(), R.string.no_internet, Toast.LENGTH_SHORT).show();
    }
}
