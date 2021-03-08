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
    private ArrayList<Room> rooms;

    private TextView tvGraphTitle;
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
            int n_graphs = bundle.getInt(Constants.QUANT_KEY);

            switch (id_graph){
                case Constants.TEMP_AMB_ID:
                case Constants.CO2_ID:
                case Constants.PERSON_ID:
                    measLists = (ArrayList<MeasList>) bundle.getSerializable(Constants.DATA_KEY);
                    break;
                case Constants.ROOMS_ID:
                    rooms = (ArrayList<Room>) bundle.getSerializable(Constants.DATA_KEY);
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

        tvGraphTitle = v.findViewById(R.id.tvGraphTitle);

        for(MeasList measList : measLists) {
            TextView tv = CustomTextView(measList.GetRoom());
            GraphView graph = CustomGraphView();
            currentSeries = new LineGraphSeries<>();
            gridLabel = graph.getGridLabelRenderer();
            viewport = graph.getViewport();

            currentSeries = LoadMeasurements(measList);
            graph.addSeries(currentSeries);
            series.add(currentSeries);
            viewport.setScalable(true);

            CustomAxis(measList, graph);

            if(measList.GetMeas() == Constants.TEMP_OBJ_ID)
                tv = CustomTextView(measList.GetRoom() + " - " + getString(R.string.lbl_graph_obj));
            else if(measList.GetMeas() == Constants.SPO2_ID)
                tv = CustomTextView(measList.GetRoom() + " - " + getString(R.string.lbl_graph_spo2));

            ll.addView(tv);
            ll.addView(graph);
        }
        sv.addView(ll);
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
            if(id_graph == Constants.PERSON_ID)
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

    private TextView CustomTextView(String text){
        TextView tv = new TextView(getContext());
        tv.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.poppins_medium);
        tv.setTypeface(typeface);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setText(text.toUpperCase());

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
        tvGraphTitle.setText(R.string.lbl_subject);
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
        tvGraphTitle.setText(R.string.lbl_graph_co2);
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(400);
        viewport.setMaxY(2000);
    }

    private void CustomAmbGraph() {
        currentSeries.setColor(Color.BLUE);
        gridLabel.setVerticalAxisTitle(getString(R.string.lbl_axis_temp));
        tvGraphTitle.setText(R.string.lbl_graph_amb);
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