package com.cruxlab.parkurbn.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cruxlab.parkurbn.ParkUrbnApplication;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.adapters.ParkingHistoryAdapter;
import com.cruxlab.parkurbn.api.LoadHistoryManager;
import com.cruxlab.parkurbn.db.ParkUrbnDatabase;
import com.cruxlab.parkurbn.model.History;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ParkingHistoryActivity extends BaseActivity {

    @BindView(R.id.rv_parking_history)
    RecyclerView rvParkingHistory;
    @BindView(R.id.tv_no_results)
    TextView tvNoResults;

    private ParkingHistoryAdapter mAdapter;
    private ParkUrbnDatabase mParkUrbnDb;
    private LoadHistoryManager mLoadHistoryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_history);
        ButterKnife.bind(this);

        mParkUrbnDb = ParkUrbnApplication.get().getDb();
        mLoadHistoryManager = LoadHistoryManager.get();

        rvParkingHistory.setHasFixedSize(true);
        rvParkingHistory.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new ParkingHistoryAdapter(ParkingHistoryActivity.this);
        rvParkingHistory.setAdapter(mAdapter);

        showLoader();
        new FetchFromDbAsyncTask().execute();
        loadData();
    }

    @OnClick(R.id.ibtn_nav_btn)
    void onNavBtnClick() {
        onBackPressed();
    }

    private void loadData() {
        mLoadHistoryManager.loadHistory(new LoadHistoryManager.ILoadHistoryCallback() {
            @Override
            public void onSuccess() {
                new FetchFromDbAsyncTask().execute();
            }

            @Override
            public void onFailure() {
                showNoHistoryLabel();
            }
        });
    }

    private void showNoHistoryLabel() {
        hideLoader();
        tvNoResults.setVisibility(View.VISIBLE);
    }

    private class FetchFromDbAsyncTask extends AsyncTask<Void, Void, List<History>> {

        @Override
        protected List<History> doInBackground(Void... params) {
            return mParkUrbnDb.historyDao().getAll();
        }

        @Override
        protected void onPostExecute(List<History> result) {
            if (!result.isEmpty()) {
                tvNoResults.setVisibility(View.GONE);
                hideLoader();
            } else if (mLoadHistoryManager.isAlreadyLoadedOnce()) {
                showNoHistoryLabel();
            }
            mAdapter.setHistory(result);
            mAdapter.notifyDataSetChanged();
        }
    }
}