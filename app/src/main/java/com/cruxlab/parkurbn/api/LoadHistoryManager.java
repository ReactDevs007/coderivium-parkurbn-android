package com.cruxlab.parkurbn.api;

import android.os.AsyncTask;

import com.cruxlab.parkurbn.ParkUrbnApplication;
import com.cruxlab.parkurbn.db.ParkUrbnDatabase;
import com.cruxlab.parkurbn.model.History;
import com.cruxlab.parkurbn.model.response.ErrorResponse;
import com.cruxlab.parkurbn.model.response.HistoryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by alla on 7/4/17.
 */

public class LoadHistoryManager {

    public interface ILoadHistoryCallback {

        void onSuccess();

        void onFailure();

    }

    private static LoadHistoryManager instance;

    private ParkUrbnApi parkUrbnApi;
    private ParkUrbnDatabase parkUrbnDb;
    private ILoadHistoryCallback callback;

    private boolean isAlreadyLoadedOnce;

    private LoadHistoryManager() {
        parkUrbnApi = ParkUrbnApplication.get().getParkUrbnApi();
        parkUrbnDb = ParkUrbnApplication.get().getDb();
    }

    public static LoadHistoryManager get() {
        if (instance == null) {
            instance = new LoadHistoryManager();
        }
        return instance;
    }

    public void loadHistory(final ILoadHistoryCallback cb) {
        this.callback = cb;
        Call<HistoryResponse> call = parkUrbnApi.getParkingHistory();
        call.enqueue(new ResponseCallback<HistoryResponse>() {
            @Override
            public void handleResponse(Response<HistoryResponse> response) {
                new SaveToDbAsyncTask(response.body().getTransactions()).execute();
                isAlreadyLoadedOnce = true;
            }

            @Override
            public void onFailure(Call<HistoryResponse> call, Throwable t) {
                isAlreadyLoadedOnce = true;
                if (callback != null) callback.onFailure();
            }

            @Override
            protected void showErrorMessage(ErrorResponse response) {
                isAlreadyLoadedOnce = true;
                if (callback != null) callback.onFailure();
            }
        });
    }

    public boolean isAlreadyLoadedOnce() {
        return isAlreadyLoadedOnce;
    }

    private class SaveToDbAsyncTask extends AsyncTask<Void, Void, Void> {

        private List<History> history;

        SaveToDbAsyncTask(List<History> history) {
            this.history = history;
        }

        @Override
        protected Void doInBackground(Void... params) {
            parkUrbnDb.historyDao().deleteAll();
            parkUrbnDb.historyDao().insertAll(history);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (callback != null) callback.onSuccess();
        }
    }
}
