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

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private Activity activity;
    private IComFragments iComFragments;

    private RelativeLayout btnRoom;
    private RelativeLayout btnPerson;
    private RelativeLayout btnCo2;
    private RelativeLayout btnRooms;

    private final static int TEMP_OBJ = 1;
    private final static int TEMP_AMB = 2;
    private final static int CO2 = 3;
    private final static int SPO2 = 4;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnRoom = view.findViewById(R.id.btnRoom);
        btnPerson = view.findViewById(R.id.btnPerson);
        btnCo2 = view.findViewById(R.id.btnCo2);
        btnRooms = view.findViewById(R.id.btnRooms);

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
            activity = (Activity) context;
            iComFragments = (IComFragments) activity;
        }
    }

    public void OnClick(View view){
        switch (view.getId()){
            case R.id.btnRoom:
                iComFragments.SendData(TEMP_AMB);
                break;
            case R.id.btnPerson:
                iComFragments.SendData(TEMP_OBJ);
                break;
            case R.id.btnCo2:
                iComFragments.SendData(CO2);
                break;
            case R.id.btnRooms:
                iComFragments.SendData(0);
                break;
        }
    }
}