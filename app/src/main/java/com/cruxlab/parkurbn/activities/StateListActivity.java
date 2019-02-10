package com.cruxlab.parkurbn.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.adapters.StateListAdapter;
import com.cruxlab.parkurbn.consts.BundleArguments;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StateListActivity extends AppCompatActivity {

    @BindView(R.id.lv_states)
    ListView statesListView;

    private StateListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state_list);
        ButterKnife.bind(this);
        mAdapter = new StateListAdapter(this);
        statesListView.setAdapter(mAdapter);
    }

    @OnClick(R.id.btn_cancel)
    void back() {
        onBackPressed();
    }

    public void setStateResult(String title, String id) {
        Intent intent = new Intent();
        intent.putExtra(BundleArguments.STATE_TITLE, title);
        intent.putExtra(BundleArguments.STATE_ID, id);
        setResult(RESULT_OK, intent);
        finish();
    }
}
