package com.cruxlab.parkurbn.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cruxlab.parkurbn.R;

public class TutorialFirstFragment extends Fragment {

    public static TutorialFirstFragment newInstance() {
        return new TutorialFirstFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tutorial_first, container, false);
    }

}
