package com.example.mqttandroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private IComFragments iComFragments;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RelativeLayout btnRooms = view.findViewById(R.id.btnRooms);
        RelativeLayout btnPeople = view.findViewById(R.id.btnPeople);
        RelativeLayout btnMap = view.findViewById(R.id.btnMap);
        RelativeLayout btnSettings = view.findViewById(R.id.btnSettings);

        btnRooms.setOnClickListener(this::OnClick);
        btnPeople.setOnClickListener(this::OnClick);
        btnMap.setOnClickListener(this::OnClick);
        btnSettings.setOnClickListener(this::OnClick);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof Activity){
            Activity activity = (Activity) context;
            iComFragments = (IComFragments) activity;
        }
    }

    public void OnClick(View view){
        switch (view.getId()){
            case R.id.btnRooms:
                iComFragments.BtnClicked(Constants.DISPO_HABITACION);
                break;
            case R.id.btnPeople:
                iComFragments.BtnClicked(Constants.DISPO_PERSONA);
                break;
            case R.id.btnMap:
                iComFragments.BtnClicked(Constants.MAP_ID);
                break;
            case R.id.btnSettings:
                iComFragments.BtnClicked(Constants.SETTINGS_ID);
                break;
        }
    }
}