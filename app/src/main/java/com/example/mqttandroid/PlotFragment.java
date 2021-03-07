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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class PlotFragment extends Fragment implements IComData{

    private int id_graph;
    private ArrayList<MeasList> measLists;

    private TextView tvGraphTitle;
    private GridLabelRenderer gridLabel;
    private Viewport viewport;

    private LineGraphSeries<DataPoint> currentSeries;
    private ArrayList<LineGraphSeries<DataPoint>> graphSeries;

    public PlotFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        measLists = new ArrayList<>();
        graphSeries = new ArrayList<>();

        if (getArguments() != null) {
            Bundle bundle = getArguments();

            int n_graphs = bundle.getInt(Constants.QUANT_KEY);
            for(int i = 0; i< n_graphs; i++){
                MeasList measList = (MeasList) bundle.getSerializable(Constants.DATA_KEY + i);
                measLists.add(measList);
            }
            id_graph = bundle.getInt(Constants.CASE_KEY);
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

        for(MeasList measList : measLists){
            TextView tv = CustomTextView(measList.GetRoom());
            GraphView graph = CustomGraphView();
            currentSeries = new LineGraphSeries<>();
            gridLabel = graph.getGridLabelRenderer();
            viewport = graph.getViewport();

            ArrayList<Measurement> arr = measList.GetList();
            for (int i=0; i < arr.size(); i++){
                Measurement m = measList.GetList().get(i);
                currentSeries.appendData(new DataPoint(m.GetSample(), m.GetValue()), true, 20);
            }

            graph.addSeries(currentSeries);
            graphSeries.add(currentSeries);
            viewport.setScrollable(true);
            tvGraphTitle = v.findViewById(R.id.tvGraphTitle);

            switch (id_graph){
                case Constants.TEMP_OBJ_ID:
                    CustomSamplesGraph();
                    CustomObjGraph();
                    break;
                case Constants.TEMP_AMB_ID:
                    CustomTimeGraph();
                    CustomAmbGraph();
                    break;
                case Constants.CO2_ID:
                    CustomTimeGraph();
                    CustomCO2Graph();
                    break;
                case Constants.SPO2_ID:
                    CustomSamplesGraph();
                    CustomSPO2Graph();
                    break;
                default:
                    tvGraphTitle.setText("Graficos en desarrollo...");
                    break;
            }
            ll.addView(tv);
            ll.addView(graph);
        }
        sv.addView(ll);
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
        currentSeries.setDrawDataPoints(true);
        currentSeries.setDataPointsRadius(10);
        currentSeries.setThickness(1);
        gridLabel.setHorizontalAxisTitle(getString(R.string.lbl_axis_samples));
    }

    private void CustomTimeGraph(){
        gridLabel.setHorizontalAxisTitle(getString(R.string.lbl_axis_time));
    }

    private void CustomSPO2Graph() {
        currentSeries.setColor(Color.GREEN);
        tvGraphTitle.setText(R.string.lbl_graph_spo2);
    }

    private void CustomCO2Graph() {
        currentSeries.setColor(Color.DKGRAY);
        tvGraphTitle.setText(R.string.lbl_graph_co2);
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(400);
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
        tvGraphTitle.setText(R.string.lbl_graph_obj);
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(20);
        viewport.setMaxY(40);
    }

    @Override
    public void MeasArrived(String id_room, int id_meas, Measurement measurement) {
        for(int i=0; i<measLists.size();i++){
            MeasList measList = measLists.get(i);
            if(id_room.equals(measList.GetRoom())){
                if(id_meas == id_graph)
                    graphSeries.get(i).appendData(new DataPoint(measurement.GetSample(), measurement.GetValue()), true, 20);
            }
        }

    }
}