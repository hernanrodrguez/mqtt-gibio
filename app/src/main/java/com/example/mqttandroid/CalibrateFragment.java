package com.example.mqttandroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CalibrateFragment extends Fragment{

    private View view;
    private IComFragments iComFragments;

    public CalibrateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_calibrate, container, false);
        SetUpListeners();
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

    private void SetUpListeners(){
        RelativeLayout btnTempSubj = view.findViewById(R.id.btnCalibrateTempSubj);
        RelativeLayout btnTempAmb = view.findViewById(R.id.btnCalibrateTempAmb);
        RelativeLayout btnCO2 = view.findViewById(R.id.btnCalibrateCO2);
        RelativeLayout btnSPO2 = view.findViewById(R.id.btnCalibrateSPO2);
        RelativeLayout btnHR = view.findViewById(R.id.btnCalibrateHeartRate);
        TextView btnCorrelation = view.findViewById(R.id.btnCorrelation);

        btnTempSubj.setOnClickListener(this::OnClick);
        btnTempAmb.setOnClickListener(this::OnClick);
        btnCO2.setOnClickListener(this::OnClick);
        btnSPO2.setOnClickListener(this::OnClick);
        btnHR.setOnClickListener(this::OnClick);
        btnCorrelation.setOnClickListener(this::OnClick);
        btnCorrelation.setOnLongClickListener(this::OnLongClick);
    }

    private boolean OnLongClick(View view) {
        iComFragments.clearCalibrationData();
        return true;
    }

    public void OnClick(View v){
        switch (v.getId()){
            case R.id.btnCalibrateTempSubj:
                iComFragments.RequestMeasurement(Constants.TEMPERATURA_SUJETO);
                break;
            case R.id.btnCalibrateTempAmb:
                iComFragments.RequestMeasurement(Constants.TEMPERATURA_AMBIENTE);
                break;
            case R.id.btnCalibrateCO2:
                iComFragments.RequestMeasurement(Constants.CO2);
                break;
            case R.id.btnCalibrateSPO2:
                iComFragments.RequestMeasurement(Constants.SPO2);
                break;
            case R.id.btnCalibrateHeartRate:
                iComFragments.RequestMeasurement(Constants.FRECUENCIA_CARDIACA);
                break;
            case R.id.btnCorrelation:
                iComFragments.btnClicked(Constants.CORRELATION_ID);
                break;
            default:
                break;
        }
    }

}