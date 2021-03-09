package com.example.mqttandroid;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.SecondScale;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PlotFragment extends Fragment implements IComData{

    private int id_graph;
    private ArrayList<MeasList> measLists;
    private Room room;

    private GridLabelRenderer gridLabel;
    private Viewport viewport;

    private LineGraphSeries<DataPoint> currentSeries;
    private ArrayList<LineGraphSeries<DataPoint>> series;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    public PlotFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        series = new ArrayList<>();

        if (getArguments() != null) {
            Bundle bundle = getArguments();

            id_graph = bundle.getInt(Constants.CASE_KEY);
            measLists = new ArrayList<>();

            switch (id_graph){
                case Constants.TEMP_AMB_ID:
                case Constants.CO2_ID:
                case Constants.PERSON_ID:
                    measLists = (ArrayList<MeasList>) bundle.getSerializable(Constants.DATA_KEY);
                    break;
                case Constants.ROOMS_ID:
                    room = (Room) bundle.getSerializable(Constants.DATA_KEY);
                    break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plot, container, false);
        SetUpGraphView(view);
        return view;
    }

    private void SetUpGraphView(View v) {
        // https://github.com/jjoe64/GraphView/wiki/Documentation

        ScrollView sv = v.findViewById(R.id.scrollView);
        LinearLayout ll = CustomLinearLayout();
        CustomGraphTitle(v);

        if(id_graph == Constants.ROOMS_ID)
            measLists = LoadList();

        for(MeasList measList : measLists) {
            TextView tv = CustomTextView(id_graph, measList);

            GraphView graph = CustomGraphView();
            currentSeries = new LineGraphSeries<>();
            gridLabel = graph.getGridLabelRenderer();
            viewport = graph.getViewport();
            currentSeries = LoadMeasurements(measList);

            graph.addSeries(currentSeries);
            series.add(currentSeries);
            viewport.setScalable(true);

            CustomAxis(measList, graph);

            ll.addView(tv);
            ll.addView(graph);
        }
        sv.addView(ll);
    }

    private void CustomGraphTitle(View v){
        TextView tv = v.findViewById(R.id.tvGraphTitle);
        switch (id_graph){
            case Constants.TEMP_AMB_ID:
                tv.setText(R.string.lbl_graph_amb);
                break;
            case Constants.CO2_ID:
                tv.setText(R.string.lbl_graph_co2);
                break;
            case Constants.PERSON_ID:
                tv.setText(R.string.lbl_subject);
                break;
            case Constants.ROOMS_ID:
                tv.setText(room.GetIdRoom().toUpperCase());
                break;
        }
    }

    private ArrayList<MeasList> LoadList(){
        ArrayList<MeasList> list = new ArrayList<>();
        list.add(room.GetTAmbList());
        list.add(room.GetCo2List());
        list.add(room.GetTObjList());
        list.add(room.GetSpo2List());

        return list;
    }

    private void CustomAxis(MeasList measList, GraphView graph){
        switch (measList.GetMeas()) {
            case Constants.TEMP_OBJ_ID:
                CustomSamplesGraph();
                CustomObjGraph();
                break;
            case Constants.TEMP_AMB_ID:
                CustomTimeGraph(graph);
                CustomAmbGraph();
                break;
            case Constants.CO2_ID:
                CustomTimeGraph(graph);
                CustomCO2Graph();
                break;
            case Constants.SPO2_ID:
                CustomSamplesGraph();
                CustomSPO2Graph();
                break;
        }
    }

    private LineGraphSeries<DataPoint> LoadMeasurements(MeasList measList){
        ArrayList<Measurement> list = measList.GetList();
        LineGraphSeries<DataPoint> aux_series = new LineGraphSeries<>();
        for (Measurement m : list) {
            if(measList.GetMeas() == Constants.TEMP_OBJ_ID || measList.GetMeas() == Constants.SPO2_ID)
                aux_series.appendData(new DataPoint(m.GetSample(), m.GetValue()), true, 20);
            else
                aux_series.appendData(new DataPoint(m.GetDate(), m.GetValue()), true, 40);
        }
        return aux_series;
    }

    private LinearLayout CustomLinearLayout(){
        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );
        return ll;
    }

    private TextView CustomTextView(int id_graph, MeasList list){
        TextView tv = new TextView(getContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 50, 0, 20);
        tv.setLayoutParams(params);

        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.poppins_medium);
        tv.setTypeface(typeface);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        if(id_graph == Constants.ROOMS_ID) {
            switch (list.GetMeas()) {
                case Constants.TEMP_OBJ_ID:
                    tv.setText(R.string.lbl_graph_obj);
                    break;
                case Constants.TEMP_AMB_ID:
                    tv.setText(R.string.lbl_graph_amb);
                    break;
                case Constants.CO2_ID:
                    tv.setText(R.string.lbl_graph_co2);
                    break;
                case Constants.SPO2_ID:
                    tv.setText(R.string.lbl_graph_spo2);
                    break;
            }
        }else{
            if(list.GetMeas() == Constants.TEMP_OBJ_ID)
                tv.setText(getString(R.string.lbl_graph_room_obj, list.GetRoom().toUpperCase()));
            else if(list.GetMeas() == Constants.SPO2_ID)
                tv.setText(getString(R.string.lbl_graph_room_spo2, list.GetRoom().toUpperCase()));
            else
                tv.setText(list.GetRoom().toUpperCase());
        }
        return tv;
    }

    private GraphView CustomGraphView(){
        GraphView graph = new GraphView(getContext());
        graph.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        600
                )
        );
        return graph;
    }

    private void CustomSamplesGraph(){
        currentSeries.setDrawDataPoints(true);
        currentSeries.setDataPointsRadius(10);
        currentSeries.setThickness(1);
        gridLabel.setHorizontalAxisTitle(getString(R.string.lbl_axis_samples));
    }

    private void CustomTimeGraph(GraphView g){
        gridLabel.setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
        gridLabel.setHumanRounding(false);
        g.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if(isValueX)
                    return sdf.format(new Date((long)value));
                else
                    return super.formatLabel(value, isValueX);
            }
        });
    }

    private void CustomSPO2Graph() {
        currentSeries.setColor(Color.GREEN);
        gridLabel.setVerticalAxisTitle(getString(R.string.lbl_axis_spo2));
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(80);
        viewport.setMaxY(100);
    }

    private void CustomCO2Graph() {
        currentSeries.setColor(Color.DKGRAY);
        gridLabel.setVerticalAxisTitle(getString(R.string.lbl_axis_co2));
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(400);
        viewport.setMaxY(2000);
    }

    private void CustomAmbGraph() {
        currentSeries.setColor(Color.BLUE);
        gridLabel.setVerticalAxisTitle(getString(R.string.lbl_axis_temp));
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(40);
    }

    private void CustomObjGraph() {
        currentSeries.setColor(Color.RED);
        gridLabel.setVerticalAxisTitle(getString(R.string.lbl_axis_temp));
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(20);
        viewport.setMaxY(40);
    }

    @Override
    public void MeasArrived(String id_room, int id_meas, Measurement m) {
        for(int i=0; i<measLists.size();i++){
            MeasList measList = measLists.get(i);
            if(id_room.equals(measList.GetRoom())) {
                if(id_meas == measList.GetMeas()){
                    if(id_meas == Constants.TEMP_AMB_ID || id_meas == Constants.CO2_ID)
                        series.get(i).appendData(new DataPoint(m.GetDate(), m.GetValue()), true, 40);
                    else
                        series.get(i).appendData(new DataPoint(m.GetSample(), m.GetValue()), true, 20);
                }
            }
        }
    }

    public int GetIdGraph(){ return id_graph; }
}