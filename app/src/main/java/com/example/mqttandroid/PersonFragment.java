package com.example.mqttandroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

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
        SetUpMainView(view);
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

    private void SetUpMainView(View v){
        TextView btnHistory = v.findViewById(R.id.btnHistory);
        RelativeLayout btnSubjectTemperature = v.findViewById(R.id.btnSubjectTemperature);
        RelativeLayout btnSPO2Level = v.findViewById(R.id.btnSPO2Level);
        RelativeLayout btnHeartRate = v.findViewById(R.id.btnHeartRate);
        RelativeLayout btnRoomTemperature = v.findViewById(R.id.btnRoomTemperature);
        RelativeLayout btnCO2Level = v.findViewById(R.id.btnCO2Level);

        btnHistory.setOnClickListener(this::OnClick);
        btnSubjectTemperature.setOnClickListener(this::OnClick);
        btnSPO2Level.setOnClickListener(this::OnClick);
        btnHeartRate.setOnClickListener(this::OnClick);
        btnRoomTemperature.setOnClickListener(this::OnClick);
        btnCO2Level.setOnClickListener(this::OnClick);
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

    @Override
    public void MeasArrived(String id_room, int id_meas, Measurement measurement) {

    }
}