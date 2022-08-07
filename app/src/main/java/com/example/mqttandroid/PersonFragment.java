package com.example.mqttandroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PersonFragment extends Fragment implements IComData {

    private IComFragments iComFragments;

    private ArrayList<ArrayMediciones> arrayMediciones;
    private Dispositivo person;
    private int id_person;

    private View view;

    private RelativeLayout btnSubjectTemperature;
    private RelativeLayout btnSPO2Level;
    private RelativeLayout btnHeartRate;
    private RelativeLayout btnRoomTemperature;
    private RelativeLayout btnCO2Level;

    private TextView tvTObjValue;
    private TextView tvTObjTime;

    private TextView tvTAmbValue;
    private TextView tvTAmbTime;

    private TextView tvCO2Value;
    private TextView tvCO2Time;

    private TextView tvSPO2Value;
    private TextView tvSPO2Time;

    private TextView tvHRValue;
    private TextView tvHRTime;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    public PersonFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arrayMediciones = new ArrayList<>();
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            person = (Dispositivo) bundle.getSerializable(Constants.DATA_KEY);
            id_person = bundle.getInt(Constants.CASE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_person, container, false);
        SetUpListeners();
        SetUpLastMeasurement();
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
        TextView tvTitle = view.findViewById(R.id.tv_title_last_meas);
        TextView btnHistory = view.findViewById(R.id.btnHistory);
        btnSubjectTemperature = view.findViewById(R.id.btnSubjectTemperature);
        btnSPO2Level = view.findViewById(R.id.btnSPO2Level);
        btnHeartRate = view.findViewById(R.id.btnHeartRate);
        btnRoomTemperature = view.findViewById(R.id.btnRoomTemperature);
        btnCO2Level = view.findViewById(R.id.btnCO2Level);

        tvTitle.setText(person.getKey().toUpperCase());

        btnHistory.setOnClickListener(this::OnClick);
        btnSubjectTemperature.setOnClickListener(this::OnClick);
        btnSPO2Level.setOnClickListener(this::OnClick);
        btnHeartRate.setOnClickListener(this::OnClick);
        btnRoomTemperature.setOnClickListener(this::OnClick);
        btnCO2Level.setOnClickListener(this::OnClick);
    }

    private void SetUpLastMeasurement(){
        tvTObjValue = view.findViewById(R.id.tv_last_tobj_value);
        tvTObjTime = view.findViewById(R.id.tv_last_tobj_time);

        tvTAmbValue = view.findViewById(R.id.tv_last_tamb_value);
        tvTAmbTime = view.findViewById(R.id.tv_last_tamb_time);

        tvCO2Value = view.findViewById(R.id.tv_last_co2_value);
        tvCO2Time = view.findViewById(R.id.tv_last_co2_time);

        tvSPO2Value = view.findViewById(R.id.tv_last_spo2_value);
        tvSPO2Time = view.findViewById(R.id.tv_last_spo2_time);

        tvHRValue = view.findViewById(R.id.tv_last_hr_value);
        tvHRTime = view.findViewById(R.id.tv_last_hr_time);

        for(int id : Constants.MEAS_IDS){
            try {
                CustomBtnColor(person.getUltimaMedicion(id), id);
            } catch (Exception e){
                CustomNotMeas(id);
            }
        }
    }

    private void CustomNotMeas(int id){

        int red = ContextCompat.getColor(getActivity(), R.color.red);

        switch (id) {
            case Constants.TEMP_OBJ_ID:
                tvTObjTime.setText(getString(R.string.lbl_no_meas));
                btnSubjectTemperature.setBackgroundColor(red);
                break;
            case Constants.SPO2_ID:
                tvSPO2Time.setText(getString(R.string.lbl_no_meas));
                btnSPO2Level.setBackgroundColor(red);
                break;
            case Constants.TEMP_AMB_ID:
                tvTAmbTime.setText(getString(R.string.lbl_no_meas));
                btnRoomTemperature.setBackgroundColor(red);
                break;
            case Constants.CO2_ID:
                tvCO2Time.setText(getString(R.string.lbl_no_meas));
                btnCO2Level.setBackgroundColor(red);
                break;
            case Constants.HR_ID:
                tvHRTime.setText(getString(R.string.lbl_no_meas));
                btnHeartRate.setBackgroundColor(red);
                break;
            default:
                break;
        }
    }

    private void CustomBtnColor(Medicion m, int id) {
        double value = m.getValue();
        Date date = m.getDate();

        int red = ContextCompat.getColor(getActivity(), R.color.red);
        int green = ContextCompat.getColor(getActivity(), R.color.green_sea);

        switch (id){
            case Constants.TEMP_OBJ_ID:
                tvTObjValue.setText(getString(R.string.lbl_last_value, value, "°C"));
                tvTObjTime.setText(getString(R.string.lbl_last_time, sdf.format(date)));
                if(value > Constants.TH_TEMP)
                    btnSubjectTemperature.setBackgroundColor(red);
                else
                    btnSubjectTemperature.setBackgroundColor(green);
                break;
            case Constants.SPO2_ID:
                tvSPO2Value.setText(getString(R.string.lbl_last_value, value, "%"));
                tvSPO2Time.setText(getString(R.string.lbl_last_time, sdf.format(date)));
                if(value < Constants.TH_SPO2)
                    btnSPO2Level.setBackgroundColor(red);
                else
                    btnSPO2Level.setBackgroundColor(green);
                break;
            case Constants.TEMP_AMB_ID:
                tvTAmbValue.setText(getString(R.string.lbl_last_value, value, "°C"));
                tvTAmbTime.setText(getString(R.string.lbl_last_time, sdf.format(date)));
                if(value > 35)
                    btnRoomTemperature.setBackgroundColor(red);
                else
                    btnRoomTemperature.setBackgroundColor(green);
                break;
            case Constants.CO2_ID:
                tvCO2Value.setText(getString(R.string.lbl_last_value, value, " ppm"));
                tvCO2Time.setText(getString(R.string.lbl_last_time, sdf.format(date)));
                if(value > Constants.TH_CO2)
                    btnCO2Level.setBackgroundColor(red);
                else
                    btnCO2Level.setBackgroundColor(green);
                break;
            case Constants.HR_ID:
                tvHRValue.setText(getString(R.string.lbl_last_value, value, " bpm"));
                tvHRTime.setText(getString(R.string.lbl_last_time, sdf.format(date)));
                if(value > Constants.TH_HR)
                    btnHeartRate.setBackgroundColor(red);
                else
                    btnHeartRate.setBackgroundColor(green);
                break;
            default:
                break;
        }
    }

    public void OnClick(View v){
        switch (v.getId()){
            case R.id.btnHistory:
                iComFragments.BtnClicked(Constants.PERSON_ID, id_person);
                break;
            case R.id.btnSubjectTemperature:
                iComFragments.BtnClicked(Constants.TEMP_OBJ_ID, id_person);
                break;
            case R.id.btnSPO2Level:
                iComFragments.BtnClicked(Constants.SPO2_ID, id_person);
                break;
            case R.id.btnRoomTemperature:
                iComFragments.BtnClicked(Constants.TEMP_AMB_ID, id_person);
                break;
            case R.id.btnCO2Level:
                iComFragments.BtnClicked(Constants.CO2_ID, id_person);
                break;
            case R.id.btnHeartRate:
                iComFragments.BtnClicked(Constants.HR_ID, id_person);
                break;
            default:
                break;
        }
    }

    @Override
    public void MeasArrived(String id_room, int id_meas, Medicion medicion) {
        if(person.getKey().equals(id_room))
            CustomBtnColor(medicion, id_meas);
    }
}