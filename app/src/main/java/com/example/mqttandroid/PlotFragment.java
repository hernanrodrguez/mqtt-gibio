package com.example.mqttandroid;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.SecondScale;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PlotFragment extends Fragment implements IComData{

    private int id_graph;
    private ArrayList<MeasList> measLists;
    private Room room;

    private String real_values;
    private String meas_values;

    private GridLabelRenderer gridLabel;
    private Viewport viewport;

    private LineGraphSeries<DataPoint> currentSeries;
    private ArrayList<LineGraphSeries<DataPoint>> thresholdSeries;
    private ArrayList<LineGraphSeries<DataPoint>> series;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    public PlotFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        series = new ArrayList<>();
        thresholdSeries = new ArrayList<>();

        if (getArguments() != null) {
            Bundle bundle = getArguments();

            id_graph = bundle.getInt(Constants.CASE_KEY);
            measLists = new ArrayList<>();

            switch (id_graph){
                case Constants.TEMP_AMB_ID:
                case Constants.CO2_ID:
                case Constants.TEMP_OBJ_ID:
                case Constants.SPO2_ID:
                case Constants.HR_ID:
                    MeasList list = (MeasList) bundle.getSerializable(Constants.DATA_KEY);
                    measLists.add(list);
                    break;
                case Constants.PERSON_ID:
                    measLists = (ArrayList<MeasList>) bundle.getSerializable(Constants.DATA_KEY);
                    break;
                case Constants.ROOMS_ID:
                case Constants.PEOPLE_ID:
                    room = (Room) bundle.getSerializable(Constants.DATA_KEY);
                    break;
                case Constants.CORRELATION_ID:
                    real_values = bundle.getString(Constants.REAL_VALUES_KEY);
                    meas_values = bundle.getString(Constants.MEAS_VALUES_KEY);
                    break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plot, container, false);
        if(id_graph != Constants.CORRELATION_ID)
            SetUpGraphView(view);
        else
            SetUpCorrelationView(view);
        return view;
    }

    private void SetUpCorrelationView(View v){
        ScrollView sv = v.findViewById(R.id.scrollView);
        LinearLayout ll = CustomLinearLayout();
        CustomGraphTitle(v);

        PointsGraphSeries.CustomShape shape = (canvas, paint, x, y, dataPoint) -> {
            paint.setStrokeWidth(5);
            canvas.drawLine(x-10, y-10, x+10, y+10, paint);
            canvas.drawLine(x+10, y-10, x-10, y+10, paint);
        };

        for(String key : Constants.KEYS){
            TextView tv = CustomTextView(Constants.Key2Id(key));
            GraphView graph = CustomGraphView();
            LineGraphSeries<DataPoint> calibratedSeries = GetCalibratedLine(key);
            calibratedSeries.setColor(Color.GREEN);

            PointsGraphSeries<DataPoint> series = LoadDataPoints(key, false);
            series.setColor(Color.RED);
            series.setCustomShape(shape);

            PointsGraphSeries<DataPoint> cal_series = LoadDataPoints(key, true);
            cal_series.setColor(Color.BLUE);
            cal_series.setCustomShape(shape);

            series.setTitle(getString(R.string.lbl_meas_values));
            cal_series.setTitle(getString(R.string.lbl_cal_values));
            calibratedSeries.setTitle(getString(R.string.lbl_real_values));
            graph.getLegendRenderer().setVisible(true);
            graph.getLegendRenderer().setFixedPosition(0,0);

            gridLabel = graph.getGridLabelRenderer();
            CustomGridLabel(Constants.Key2Id(key));

            viewport = graph.getViewport();

            graph.addSeries(series);
            graph.addSeries(cal_series);
            graph.addSeries(calibratedSeries);

            viewport.setScalable(true);

            ll.addView(tv);
            ll.addView(graph);
        }
        sv.addView(ll);
    }

    private void CustomGridLabel(int id){
        switch (id){
            case Constants.TEMP_OBJ_ID:
            case Constants.TEMP_AMB_ID:
                gridLabel.setVerticalAxisTitle(getString(R.string.lbl_real_values_graph, "°C"));
                gridLabel.setHorizontalAxisTitle(getString(R.string.lbl_meas_values_graph, "°C"));
                break;
            case Constants.CO2_ID:
                gridLabel.setVerticalAxisTitle(getString(R.string.lbl_real_values_graph, "ppm"));
                gridLabel.setHorizontalAxisTitle(getString(R.string.lbl_meas_values_graph, "ppm"));
                break;
            case Constants.SPO2_ID:
                gridLabel.setVerticalAxisTitle(getString(R.string.lbl_real_values_graph, "%"));
                gridLabel.setHorizontalAxisTitle(getString(R.string.lbl_meas_values_graph, "%"));
                break;
            case Constants.HR_ID:
                gridLabel.setVerticalAxisTitle(getString(R.string.lbl_real_values_graph, "bpm"));
                gridLabel.setHorizontalAxisTitle(getString(R.string.lbl_meas_values_graph, "bpm"));
                break;
        }
    }

    private PointsGraphSeries<DataPoint> LoadDataPoints(String key, boolean calibrated){
        double factor = 1;
        PointsGraphSeries<DataPoint> ret = new PointsGraphSeries<>();
        ArrayList<Double> sorted_meas = new ArrayList<>();
        ArrayList<Double> sorted_real = new ArrayList<>();

        String[] values_meas = meas_values.split("-");
        String[] values_real = real_values.split("-");
        for(int i=0; i<values_meas.length; i++){
            String _key = values_meas[i].split(":")[0];
            String _meas_val = values_meas[i].split(":")[1];
            String _real_val = values_real[i].split(":")[1];
            if(_key.equals(key) && !_meas_val.equals("")){ // valor de interes
                sorted_meas.add(Double.parseDouble(_meas_val));
                sorted_real.add(Double.parseDouble(_real_val));
            }
        }
        Collections.sort(sorted_meas);
        Collections.sort(sorted_real);
        if(calibrated){
            double sum_meas = 0;
            double sum_real = 0;
            for(Double n : sorted_meas)
                sum_meas += n;
            for(Double n : sorted_real)
                sum_real += n;
            factor = sum_real/sum_meas;
        }
        for(int i=0; i<sorted_meas.size(); i++)
            ret.appendData(new DataPoint(sorted_meas.get(i)*factor, sorted_real.get(i)), true, 40);

        return ret;
    }

    private LineGraphSeries<DataPoint> GetCalibratedLine(String key){
        double min = 0;
        double max = 0;
        ArrayList<Double> list = new ArrayList<>();

        String[] values_array = real_values.split("-");

        for(String str : values_array){
            String _key = str.split(":")[0];
            String _val = str.split(":")[1];
            if(_key.equals(key) && !_val.equals(""))
                list.add(Double.parseDouble(_val));
        }
        try{
            min = Collections.min(list);
            max = Collections.max(list);
        } catch (Exception e){
            min = 0;
            max = 0;
        }
        return new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(min, min),
                new DataPoint(max, max)
        });
    }

    private void SetUpGraphView(View v) {
        // https://github.com/jjoe64/GraphView/wiki/Documentation

        ScrollView sv = v.findViewById(R.id.scrollView);
        LinearLayout ll = CustomLinearLayout();
        CustomGraphTitle(v);

        if(id_graph == Constants.ROOMS_ID || id_graph == Constants.PEOPLE_ID)
            measLists = LoadList();

        for(MeasList measList : measLists) {
            TextView tv = CustomTextView(measList.GetMeas());
            LineGraphSeries<DataPoint> currentThreshold;

            GraphView graph = CustomGraphView();
            currentSeries = new LineGraphSeries<>();
            gridLabel = graph.getGridLabelRenderer();
            viewport = graph.getViewport();
            currentSeries = LoadMeasurements(measList);
            currentThreshold = SetThresholdLine(measList.GetMeas(), currentSeries);

            graph.addSeries(currentSeries);
            graph.addSeries(currentThreshold);
            series.add(currentSeries);
            thresholdSeries.add(currentThreshold);

            viewport.setScalable(true);
            CustomAxis(measList, graph);

            ll.addView(tv);
            ll.addView(graph);
        }
        sv.addView(ll);
    }

    private void CustomGraphTitle(View v){
        TextView tv = v.findViewById(R.id.tvGraphTitle);
        if(id_graph == Constants.ROOMS_ID || id_graph == Constants.PEOPLE_ID)
            tv.setText(room.GetIdRoom().toUpperCase());
        else if(id_graph == Constants.CORRELATION_ID)
            tv.setText(getString(R.string.lbl_corr).toUpperCase());
        else
            tv.setText(measLists.get(0).GetRoom().toUpperCase());
    }

    private ArrayList<MeasList> LoadList(){
        ArrayList<MeasList> list = new ArrayList<>();
        if(id_graph == Constants.ROOMS_ID) {
            list.add(room.GetTAmbList());
            list.add(room.GetCo2List());
            list.add(room.GetTObjList());
            list.add(room.GetSpo2List());
            list.add(room.GetHRList());
        } else if(id_graph == Constants.PEOPLE_ID){
            list.add(room.GetTObjList());
            list.add(room.GetSpo2List());
            list.add(room.GetHRList());
            list.add(room.GetTAmbList());
            list.add(room.GetCo2List());
        }
        return list;
    }

    private void CustomAxis(MeasList measList, GraphView graph){
        switch (measList.GetMeas()) {
            case Constants.TEMP_OBJ_ID:
                if(id_graph != Constants.ROOMS_ID)
                    CustomTimeGraph(graph);
                else
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
                if(id_graph != Constants.ROOMS_ID)
                    CustomTimeGraph(graph);
                else
                    CustomSamplesGraph();
                CustomSPO2Graph();
                break;
            case Constants.HR_ID:
                if(id_graph != Constants.ROOMS_ID)
                    CustomTimeGraph(graph);
                else
                    CustomSamplesGraph();
                CustomHRGraph();
                break;
            default:
                break;
        }
    }

    private LineGraphSeries<DataPoint> LoadMeasurements(MeasList measList){
        ArrayList<Measurement> list = measList.GetList();
        LineGraphSeries<DataPoint> aux_series = new LineGraphSeries<>();
        for (Measurement m : list) {
            switch (id_graph){
                case Constants.ROOMS_ID:
                    if(measList.GetMeas() == Constants.TEMP_OBJ_ID || measList.GetMeas() == Constants.SPO2_ID || measList.GetMeas() == Constants.HR_ID)
                        aux_series.appendData(new DataPoint(m.GetSample(), m.GetValue()), true, 20);
                    else
                        aux_series.appendData(new DataPoint(m.GetDate(), m.GetValue()), true, 40);
                    break;
                case Constants.PEOPLE_ID:
                case Constants.TEMP_OBJ_ID:
                case Constants.TEMP_AMB_ID:
                case Constants.SPO2_ID:
                case Constants.CO2_ID:
                case Constants.HR_ID:
                    aux_series.appendData(new DataPoint(m.GetDate(), m.GetValue()), true, 40);
                    break;
                default:
                    break;
            }
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

    private TextView CustomTextView(int id_graph){
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

        switch (id_graph) {
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
            case Constants.HR_ID:
                tv.setText(R.string.lbl_graph_hr);
                break;
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
        currentSeries.setColor(Color.rgb(0,100,0));
        gridLabel.setVerticalAxisTitle(getString(R.string.lbl_axis_spo2));
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(80);
        viewport.setMaxY(100);
    }

    private void CustomCO2Graph() {
        currentSeries.setColor(Color.rgb(105,105,105));
        gridLabel.setVerticalAxisTitle(getString(R.string.lbl_axis_co2));
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(400);
        viewport.setMaxY(2000);
    }

    private void CustomAmbGraph() {
        currentSeries.setColor(Color.rgb(70,130,180));
        gridLabel.setVerticalAxisTitle(getString(R.string.lbl_axis_temp));
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(40);
    }

    private void CustomObjGraph() {
        currentSeries.setColor(Color.rgb(255,69,0));
        gridLabel.setVerticalAxisTitle(getString(R.string.lbl_axis_temp));
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(25);
        viewport.setMaxY(45);
    }

    private void CustomHRGraph() {
        currentSeries.setColor(Color.rgb(114,188,212));
        gridLabel.setVerticalAxisTitle(getString(R.string.lbl_axis_hr));
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(40);
        viewport.setMaxY(120);
    }

    private LineGraphSeries<DataPoint> SetThresholdLine(int id, LineGraphSeries<DataPoint> lgs){
        double min_x = lgs.getLowestValueX();
        double max_x = lgs.getHighestValueX();
        double threshold;

        switch (id){
            case Constants.TEMP_OBJ_ID:
                threshold = Constants.TH_TEMP;
                break;
            case Constants.SPO2_ID:
                threshold = Constants.TH_SPO2;
                break;
            case Constants.HR_ID:
                threshold = Constants.TH_HR;
                break;
            default:
                threshold = -1;
                break;
        }
        LineGraphSeries<DataPoint> lgseries = new LineGraphSeries<>(
                new DataPoint[]{
                        new DataPoint(min_x, threshold),
                        new DataPoint(max_x, threshold)
                }
        );
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setPathEffect(new DashPathEffect(new float[]{15, 5}, 0));
        paint.setColor(Color.RED);
        lgseries.setDrawAsPath(true);
        lgseries.setCustomPaint(paint);

        return lgseries;
    }

    private void UpdateThreshold(int index, int id_meas, Measurement m){
        if(id_graph == Constants.ROOMS_ID){
            switch (id_meas) {
                case Constants.TEMP_OBJ_ID:
                    thresholdSeries.get(index).appendData(new DataPoint(m.GetSample(), Constants.TH_TEMP), true, 20);
                    break;
                case Constants.SPO2_ID:
                    thresholdSeries.get(index).appendData(new DataPoint(m.GetSample(), Constants.TH_SPO2), true, 20);
                    break;
                case Constants.HR_ID:
                    thresholdSeries.get(index).appendData(new DataPoint(m.GetSample(), Constants.TH_HR), true, 20);
                    break;
            }
        } else {
            switch (id_meas) {
                case Constants.TEMP_OBJ_ID:
                    thresholdSeries.get(index).appendData(new DataPoint(m.GetDate(), Constants.TH_TEMP), true, 20);
                    break;
                case Constants.SPO2_ID:
                    thresholdSeries.get(index).appendData(new DataPoint(m.GetDate(), Constants.TH_SPO2), true, 20);
                    break;
                case Constants.HR_ID:
                    thresholdSeries.get(index).appendData(new DataPoint(m.GetDate(), Constants.TH_HR), true, 20);
                    break;
            }
        }
    }

    @Override
    public void MeasArrived(String id_room, int id_meas, Measurement m) {
        for(int i=0; i<measLists.size();i++){
            MeasList measList = measLists.get(i);
            if(id_room.equals(measList.GetRoom())) {
                if(id_meas == measList.GetMeas()){
                    if(id_graph == Constants.ROOMS_ID){
                        if(id_meas == Constants.TEMP_AMB_ID || id_meas == Constants.CO2_ID)
                            series.get(i).appendData(new DataPoint(m.GetDate(), m.GetValue()), true, 40);
                        else {
                            series.get(i).appendData(new DataPoint(m.GetSample(), m.GetValue()), true, 20);
                            UpdateThreshold(i, id_meas, m);
                        }
                    } else {
                        series.get(i).appendData(new DataPoint(m.GetDate(), m.GetValue()), true, 40);
                        if(id_meas != Constants.TEMP_AMB_ID && id_meas != Constants.CO2_ID)
                            UpdateThreshold(i, id_meas, m);
                    }
                }
            }
        }
    }

    public int GetIdGraph(){ return id_graph; }
}