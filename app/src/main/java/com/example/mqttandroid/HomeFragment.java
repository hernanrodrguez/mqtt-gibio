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

        RelativeLayout btnRoom = view.findViewById(R.id.btnRoom);
        RelativeLayout btnPerson = view.findViewById(R.id.btnPerson);
        RelativeLayout btnCo2 = view.findViewById(R.id.btnCo2);
        RelativeLayout btnRooms = view.findViewById(R.id.btnRooms);

        btnRoom.setOnClickListener(this::OnClick);
        btnPerson.setOnClickListener(this::OnClick);
        btnCo2.setOnClickListener(this::OnClick);
        btnRooms.setOnClickListener(this::OnClick);

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
            case R.id.btnRoom:
                iComFragments.SendData(Constants.TEMP_AMB_ID);
                break;
            case R.id.btnPerson:
                iComFragments.SendData(Constants.PERSON_ID);
                break;
            case R.id.btnCo2:
                iComFragments.SendData(Constants.CO2_ID);
                break;
            case R.id.btnRooms:
                iComFragments.SendData(Constants.ROOMS_ID);
                break;
        }
    }
}