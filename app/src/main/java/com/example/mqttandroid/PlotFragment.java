package com.example.mqttandroid;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlotFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlotFragment extends Fragment {

    private TextView tvDummy;

    private String data;
    private static final String DATA_KEY = "Data";

    public PlotFragment() {
        // Required empty public constructor
    }

    public static PlotFragment newInstance(String param1) {
        PlotFragment fragment = new PlotFragment();
        Bundle args = new Bundle();
        args.putString(DATA_KEY, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            data = getArguments().getString(DATA_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plot, container, false);

        tvDummy = view.findViewById(R.id.tvDummy);
        tvDummy.setText(data);

        return view;
    }
}