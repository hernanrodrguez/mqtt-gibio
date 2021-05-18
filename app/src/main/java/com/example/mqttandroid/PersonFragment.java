package com.example.mqttandroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
        View view = inflater.inflate(R.layout.fragment_person, container, false);
        SetUpListeners(view);
        SetUpLastMeasurement(view);
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

    private void SetUpListeners(View v){
        TextView tvTitle = v.findViewById(R.id.tv_title_last_meas);
        TextView btnHistory = v.findViewById(R.id.btnHistory);
        RelativeLayout btnSubjectTemperature = v.findViewById(R.id.btnSubjectTemperature);
        RelativeLayout btnSPO2Level = v.findViewById(R.id.btnSPO2Level);
        RelativeLayout btnHeartRate = v.findViewById(R.id.btnHeartRate);
        RelativeLayout btnRoomTemperature = v.findViewById(R.id.btnRoomTemperature);
        RelativeLayout btnCO2Level = v.findViewById(R.id.btnCO2Level);

        tvTitle.setText(getString(R.string.lbl_last_meas, person.GetIdRoom().toUpperCase()));

        btnHistory.setOnClickListener(this::OnClick);
        btnSubjectTemperature.setOnClickListener(this::OnClick);
        btnSPO2Level.setOnClickListener(this::OnClick);
        btnHeartRate.setOnClickListener(this::OnClick);
        btnRoomTemperature.setOnClickListener(this::OnClick);
        btnCO2Level.setOnClickListener(this::OnClick);
    }

    private void SetUpLastMeasurement(View v){
        TextView tvTObjDesc = v.findViewById(R.id.tv_last_tobj);
        TextView tvTAmbDesc = v.findViewById(R.id.tv_last_tamb);
        TextView tvCO2Desc = v.findViewById(R.id.tv_last_co2);
        TextView tvSPO2Desc = v.findViewById(R.id.tv_last_spo2);
        TextView tvHRDesc = v.findViewById(R.id.tv_last_hr);

        RelativeLayout btnSubjectTemperature = v.findViewById(R.id.btnSubjectTemperature);
        RelativeLayout btnSPO2Level = v.findViewById(R.id.btnSPO2Level);
        RelativeLayout btnHeartRate = v.findViewById(R.id.btnHeartRate);
        RelativeLayout btnRoomTemperature = v.findViewById(R.id.btnRoomTemperature);
        RelativeLayout btnCO2Level = v.findViewById(R.id.btnCO2Level);

        CustomBtnColor(btnSubjectTemperature, tvTObjDesc, Constants.TEMP_OBJ_ID);
        CustomBtnColor(btnSPO2Level, tvSPO2Desc, Constants.SPO2_ID);
        CustomBtnColor(btnRoomTemperature, tvTAmbDesc, Constants.TEMP_AMB_ID);
        CustomBtnColor(btnCO2Level, tvCO2Desc, Constants.CO2_ID);
    }

    private void CustomBtnColor(RelativeLayout btn, TextView tv, int id) {
        Measurement m = person.GetLastMeasurement(id);
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
                if(value > 30)
                    btn.setBackgroundColor(red);
                else
                    btn.setBackgroundColor(green);
                break;
            case Constants.CO2_ID:
                tv.setText(getString(R.string.lbl_last_desc, value, " ppm" ,sdf.format(date)));
                if(value > 1000)
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
            default:
                break;
        }
    }

    private void UpdateLastMeasurement(int id, Measurement m){

    }

    @Override
    public void MeasArrived(String id_room, int id_meas, Measurement measurement) {
        if(person.GetIdRoom().equals(id_room))
            UpdateLastMeasurement(id_meas, measurement);
    }
}