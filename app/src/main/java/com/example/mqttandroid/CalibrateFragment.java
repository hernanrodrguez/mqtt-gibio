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
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalibrateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalibrateFragment extends Fragment{

    private View view;
    private IComFragments iComFragments;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CalibrateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalibrateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalibrateFragment newInstance(String param1, String param2) {
        CalibrateFragment fragment = new CalibrateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
    }

    public void OnClick(View v){
        switch (v.getId()){
            case R.id.btnCalibrateTempSubj:
                iComFragments.RequestMeasurement(Constants.TEMP_OBJ_ID);
                //Toast.makeText(getContext(), "Request Temp Subj", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnCalibrateTempAmb:
                iComFragments.RequestMeasurement(Constants.TEMP_AMB_ID);
                //Toast.makeText(getContext(), "Request Temp Amb", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnCalibrateCO2:
                iComFragments.RequestMeasurement(Constants.CO2_ID);
                //Toast.makeText(getContext(), "Request CO2", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnCalibrateSPO2:
                iComFragments.RequestMeasurement(Constants.SPO2_ID);
                //Toast.makeText(getContext(), "Request SPO2", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnCalibrateHeartRate:
                iComFragments.RequestMeasurement(Constants.HR_ID);
                //Toast.makeText(getContext(), "Request HR", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnCorrelation:
                iComFragments.BtnClicked(Constants.CORRELATION_ID);
                //Toast.makeText(getContext(), "Show diagrams", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}