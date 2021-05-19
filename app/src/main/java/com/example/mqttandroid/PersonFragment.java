package com.example.mqttandroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PersonFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonFragment extends Fragment implements IComData {

    private IComFragments iComFragments;

    private ArrayList<MeasList> measLists;
    private Room person;
    private int id_person;

    private View view;

    private RelativeLayout btnSubjectTemperature;
    private RelativeLayout btnSPO2Level;
    private RelativeLayout btnHeartRate;
    private RelativeLayout btnRoomTemperature;
    private RelativeLayout btnCO2Level;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PersonFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PersonFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PersonFragment newInstance(String param1, String param2) {
        PersonFragment fragment = new PersonFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        measLists = new ArrayList<>();
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            person = (Room) bundle.getSerializable(Constants.DATA_KEY);
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

        tvTitle.setText(getString(R.string.lbl_last_meas, person.GetIdRoom().toUpperCase()));

        btnHistory.setOnClickListener(this::OnClick);
        btnSubjectTemperature.setOnClickListener(this::OnClick);
        btnSPO2Level.setOnClickListener(this::OnClick);
        btnHeartRate.setOnClickListener(this::OnClick);
        btnRoomTemperature.setOnClickListener(this::OnClick);
        btnCO2Level.setOnClickListener(this::OnClick);
    }

    private void SetUpLastMeasurement(){
        TextView tvTObjDesc = view.findViewById(R.id.tv_last_tobj);
        TextView tvTAmbDesc = view.findViewById(R.id.tv_last_tamb);
        TextView tvCO2Desc = view.findViewById(R.id.tv_last_co2);
        TextView tvSPO2Desc = view.findViewById(R.id.tv_last_spo2);
        TextView tvHRDesc = view.findViewById(R.id.tv_last_hr);

        btnSubjectTemperature = view.findViewById(R.id.btnSubjectTemperature);
        btnSPO2Level = view.findViewById(R.id.btnSPO2Level);
        btnHeartRate = view.findViewById(R.id.btnHeartRate);
        btnRoomTemperature = view.findViewById(R.id.btnRoomTemperature);
        btnCO2Level = view.findViewById(R.id.btnCO2Level);

        CustomBtnColor(btnSubjectTemperature, tvTObjDesc, person.GetLastMeasurement(Constants.TEMP_OBJ_ID), Constants.TEMP_OBJ_ID);
        CustomBtnColor(btnSPO2Level, tvSPO2Desc, person.GetLastMeasurement(Constants.SPO2_ID), Constants.SPO2_ID);
        CustomBtnColor(btnRoomTemperature, tvTAmbDesc, person.GetLastMeasurement(Constants.TEMP_AMB_ID), Constants.TEMP_AMB_ID);
        CustomBtnColor(btnCO2Level, tvCO2Desc, person.GetLastMeasurement(Constants.CO2_ID), Constants.CO2_ID);
        CustomBtnColor(btnHeartRate, tvHRDesc, person.GetLastMeasurement(Constants.HR_ID), Constants.HR_ID);
    }

    private void CustomBtnColor(RelativeLayout btn, TextView tv, Measurement m, int id) {
        double value = m.GetValue();
        Date date = m.GetDate();

        int red = ContextCompat.getColor(getActivity(), R.color.red);
        int green = ContextCompat.getColor(getActivity(), R.color.green_sea);

        switch (id){
            case Constants.TEMP_OBJ_ID:
                tv.setText(getString(R.string.lbl_last_desc, value, "°C" ,sdf.format(date)));
                if(value > Constants.TH_TEMP)
                    btn.setBackgroundColor(red);
                else
                    btn.setBackgroundColor(green);
                break;
            case Constants.SPO2_ID:
                tv.setText(getString(R.string.lbl_last_desc, value, "%" ,sdf.format(date)));
                if(value < Constants.TH_SPO2)
                    btn.setBackgroundColor(red);
                else
                    btn.setBackgroundColor(green);
                break;
            case Constants.TEMP_AMB_ID:
                tv.setText(getString(R.string.lbl_last_desc, value, "°C" ,sdf.format(date)));
                if(value > 35)
                    btn.setBackgroundColor(red);
                else
                    btn.setBackgroundColor(green);
                break;
            case Constants.CO2_ID:
                tv.setText(getString(R.string.lbl_last_desc, value, " ppm" ,sdf.format(date)));
                if(value > Constants.TH_CO2)
                    btn.setBackgroundColor(red);
                else
                    btn.setBackgroundColor(green);
                break;
            case Constants.HR_ID:
                tv.setText(getString(R.string.lbl_last_desc, value, " bpm" ,sdf.format(date)));
                if(value > Constants.TH_HR)
                    btn.setBackgroundColor(red);
                else
                    btn.setBackgroundColor(green);
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

    private void UpdateLastMeasurement(int id, Measurement m){
        RelativeLayout rl;
        TextView tv;
        switch (id){
            case Constants.TEMP_OBJ_ID:
                rl = view.findViewById(R.id.btnSubjectTemperature);
                tv = view.findViewById(R.id.tv_last_tobj);
                CustomBtnColor(rl, tv, m, id);
                break;
            case Constants.TEMP_AMB_ID:
                rl = view.findViewById(R.id.btnRoomTemperature);
                tv = view.findViewById(R.id.tv_last_tamb);
                CustomBtnColor(rl, tv, m, id);
                break;
            case Constants.SPO2_ID:
                rl = view.findViewById(R.id.btnSPO2Level);
                tv = view.findViewById(R.id.tv_last_spo2);
                CustomBtnColor(rl, tv, m, id);
                break;
            case Constants.CO2_ID:
                rl = view.findViewById(R.id.btnCO2Level);
                tv = view.findViewById(R.id.tv_last_co2);
                CustomBtnColor(rl, tv, m, id);
                break;
            case Constants.HR_ID:
                rl = view.findViewById(R.id.btnHeartRate);
                tv = view.findViewById(R.id.tv_last_hr);
                CustomBtnColor(rl, tv, m, id);
                break;
            default:
                break;
        }
    }

    @Override
    public void MeasArrived(String id_room, int id_meas, Measurement measurement) {
        if(person.GetIdRoom().equals(id_room))
            UpdateLastMeasurement(id_meas, measurement);
    }
}