package com.cruxlab.parkurbn.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.fragments.TutorialFirstFragment;
import com.cruxlab.parkurbn.fragments.TutorialSecondFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.relex.circleindicator.CircleIndicator;

public class TutorialActivity extends AppCompatActivity {

    @BindView(R.id.vp_tutorial)
    ViewPager mPager;
    @BindView(R.id.indicator)
    CircleIndicator mIndicator;

    private TutorialFragmentPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        ButterKnife.bind(this);
        mAdapter = new TutorialFragmentPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mIndicator.setViewPager(mPager);
    }

    @OnClick(R.id.btn_skip)
    void skip() {
        Intent intent = new Intent(this, MapActivity.class);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_right_in, R.anim.slide_left_out);
        startActivity(intent, options.toBundle());
        finish();
    }

    private class TutorialFragmentPagerAdapter extends FragmentPagerAdapter {

        public TutorialFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            return position == 0 ? TutorialFirstFragment.newInstance() : TutorialSecondFragment.newInstance();
        }

    }

}
