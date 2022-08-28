package com.example.mqttandroid;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
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
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class PlotFragment extends Fragment implements IComData{

    private int id_graph;
    private ArrayList<ArrayMediciones> arrayMediciones;
    private Dispositivo dispositivo;

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
            arrayMediciones = new ArrayList<>();

            switch (id_graph){
                case Constants.TEMPERATURA_AMBIENTE:
                case Constants.CO2:
                case Constants.TEMPERATURA_SUJETO:
                case Constants.SPO2:
                case Constants.FRECUENCIA_CARDIACA:
                    ArrayMediciones list = (ArrayMediciones) bundle.getSerializable(Constants.DATA_KEY);
                    arrayMediciones.add(list);
                    break;
                case Constants.GRAFICAR_PERSONA:
                case Constants.GRAFICAR_HABITACION:
                    dispositivo = (Dispositivo) bundle.getSerializable(Constants.DATA_KEY);
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
            case Constants.TEMPERATURA_SUJETO:
            case Constants.TEMPERATURA_AMBIENTE:
                gridLabel.setVerticalAxisTitle(getString(R.string.lbl_real_values_graph, "°C"));
                gridLabel.setHorizontalAxisTitle(getString(R.string.lbl_meas_values_graph, "°C"));
                break;
            case Constants.CO2:
                gridLabel.setVerticalAxisTitle(getString(R.string.lbl_real_values_graph, "ppm"));
                gridLabel.setHorizontalAxisTitle(getString(R.string.lbl_meas_values_graph, "ppm"));
                break;
            case Constants.SPO2:
                gridLabel.setVerticalAxisTitle(getString(R.string.lbl_real_values_graph, "%"));
                gridLabel.setHorizontalAxisTitle(getString(R.string.lbl_meas_values_graph, "%"));
                break;
            case Constants.FRECUENCIA_CARDIACA:
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
            ret.appendData(new DataPoint(sorted_meas.get(i)*factor, sorted_real.get(i)), true, 500);

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

        if(id_graph == Constants.GRAFICAR_HABITACION || id_graph == Constants.GRAFICAR_PERSONA)
            arrayMediciones = LoadList();

        for(ArrayMediciones mediciones : this.arrayMediciones) {
            TextView tv = CustomTextView(mediciones.getTipoMedicion());
            LineGraphSeries<DataPoint> currentThreshold;

            GraphView graph = CustomGraphView();
            currentSeries = new LineGraphSeries<>();
            gridLabel = graph.getGridLabelRenderer();
            viewport = graph.getViewport();
            viewport.setScrollable(true);
            currentSeries = LoadMeasurements(mediciones);
            currentThreshold = SetThresholdLine(mediciones.getTipoMedicion(), currentSeries);

            graph.addSeries(currentSeries);
            graph.addSeries(currentThreshold);
            series.add(currentSeries);
            thresholdSeries.add(currentThreshold);

            //viewport.setScalable(true);
            CustomAxis(mediciones, graph);

            ll.addView(tv);
            ll.addView(graph);
        }
        sv.addView(ll);
    }

    private void CustomGraphTitle(View v){
        TextView tv = v.findViewById(R.id.tvGraphTitle);
        if(id_graph == Constants.GRAFICAR_HABITACION || id_graph == Constants.GRAFICAR_PERSONA)
            tv.setText(dispositivo.getKey().toUpperCase());
        else if(id_graph == Constants.CORRELATION_ID)
            tv.setText(getString(R.string.lbl_corr).toUpperCase());
        /*else
            tv.setText(arrayMediciones.get(0).getKeyDispositivo().toUpperCase());*/
    }

    private ArrayList<ArrayMediciones> LoadList(){
        ArrayList<ArrayMediciones> list = new ArrayList<>();
        if(id_graph == Constants.GRAFICAR_HABITACION) {
            list.add(dispositivo.getTAmbArray());
            list.add(dispositivo.getCO2Array());
            list.add(dispositivo.getTObjArray());
            list.add(dispositivo.getSpo2Array());
            list.add(dispositivo.getHRArray());
        } else if(id_graph == Constants.GRAFICAR_PERSONA){
            list.add(dispositivo.getTObjArray());
            list.add(dispositivo.getSpo2Array());
            list.add(dispositivo.getHRArray());
            list.add(dispositivo.getTAmbArray());
            list.add(dispositivo.getCO2Array());
        }
        return list;
    }

    private void CustomAxis(ArrayMediciones arrayMediciones, GraphView graph){
        switch (arrayMediciones.getTipoMedicion()) {
            case Constants.TEMPERATURA_SUJETO:
                if(id_graph != Constants.GRAFICAR_HABITACION)
                    CustomTimeGraph(graph);
                else
                    CustomSamplesGraph();
                CustomObjGraph();
                break;
            case Constants.TEMPERATURA_AMBIENTE:
                CustomTimeGraph(graph);
                CustomAmbGraph();
                break;
            case Constants.CO2:
                CustomTimeGraph(graph);
                CustomCO2Graph();
                break;
            case Constants.SPO2:
                if(id_graph != Constants.GRAFICAR_HABITACION)
                    CustomTimeGraph(graph);
                else
                    CustomSamplesGraph();
                CustomSPO2Graph();
                break;
            case Constants.FRECUENCIA_CARDIACA:
                if(id_graph != Constants.GRAFICAR_HABITACION)
                    CustomTimeGraph(graph);
                else
                    CustomSamplesGraph();
                CustomHRGraph();
                break;
            default:
                break;
        }
    }

    private LineGraphSeries<DataPoint> LoadMeasurements(ArrayMediciones arrayMediciones){
        int i_sample = 0;
        ArrayList<Medicion> list = arrayMediciones.getMediciones();
        Collections.sort(list);
        LineGraphSeries<DataPoint> aux_series = new LineGraphSeries<>();
        for (Medicion m : list) {
            switch (id_graph) {
                case Constants.GRAFICAR_HABITACION:
                    if (arrayMediciones.getTipoMedicion() == Constants.TEMPERATURA_SUJETO || arrayMediciones.getTipoMedicion() == Constants.SPO2 || arrayMediciones.getTipoMedicion() == Constants.FRECUENCIA_CARDIACA){
                        aux_series.appendData(new DataPoint(i_sample, m.getValue()), true, 500);
                        i_sample++;
                    } else {
                        Log.println(Log.DEBUG, "LOAD MEAS", m.toString());
                        aux_series.appendData(new DataPoint(m.getDate(), m.getValue()), true, 500);
                    }
                    break;
                case Constants.GRAFICAR_PERSONA:
                case Constants.TEMPERATURA_SUJETO:
                case Constants.TEMPERATURA_AMBIENTE:
                case Constants.SPO2:
                case Constants.CO2:
                case Constants.FRECUENCIA_CARDIACA:
                    Log.d("LoadMeasurements", m.toString());
                    aux_series.appendData(new DataPoint(m.getDate(), m.getValue()), true, 500);
                    break;
                default:
                    break;
            }
        }

        if (id_graph == Constants.GRAFICAR_HABITACION && arrayMediciones.getTipoMedicion() == Constants.TEMPERATURA_SUJETO || arrayMediciones.getTipoMedicion() == Constants.SPO2 || arrayMediciones.getTipoMedicion() == Constants.FRECUENCIA_CARDIACA) {
            viewport.setXAxisBoundsManual(true);
            viewport.setMaxX(i_sample);
            viewport.setMinX(i_sample-20);
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
            case Constants.TEMPERATURA_SUJETO:
                tv.setText(R.string.lbl_graph_obj);
                break;
            case Constants.TEMPERATURA_AMBIENTE:
                tv.setText(R.string.lbl_graph_amb);
                break;
            case Constants.CO2:
                tv.setText(R.string.lbl_graph_co2);
                break;
            case Constants.SPO2:
                tv.setText(R.string.lbl_graph_spo2);
                break;
            case Constants.FRECUENCIA_CARDIACA:
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
            case Constants.TEMPERATURA_SUJETO:
                threshold = Constants.TH_TEMP;
                break;
            case Constants.SPO2:
                threshold = Constants.TH_SPO2;
                break;
            case Constants.FRECUENCIA_CARDIACA:
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

    private void UpdateThreshold(int index, int id_meas, Medicion m){
        if(id_graph == Constants.GRAFICAR_HABITACION){
            switch (id_meas) {
                case Constants.TEMPERATURA_SUJETO:
                    thresholdSeries.get(index).appendData(new DataPoint(m.getSample(), Constants.TH_TEMP), true, 500);
                    break;
                case Constants.SPO2:
                    thresholdSeries.get(index).appendData(new DataPoint(m.getSample(), Constants.TH_SPO2), true, 500);
                    break;
                case Constants.FRECUENCIA_CARDIACA:
                    thresholdSeries.get(index).appendData(new DataPoint(m.getSample(), Constants.TH_HR), true, 500);
                    break;
            }
        } else {
            switch (id_meas) {
                case Constants.TEMPERATURA_SUJETO:
                    thresholdSeries.get(index).appendData(new DataPoint(m.getDate(), Constants.TH_TEMP), true, 500);
                    break;
                case Constants.SPO2:
                    thresholdSeries.get(index).appendData(new DataPoint(m.getDate(), Constants.TH_SPO2), true, 20);
                    break;
                case Constants.FRECUENCIA_CARDIACA:
                    thresholdSeries.get(index).appendData(new DataPoint(m.getDate(), Constants.TH_HR), true, 20);
                    break;
            }
        }
    }

    @Override
    public void MeasArrived(String id_room, int id_meas, Medicion m) {
        for(int i = 0; i< arrayMediciones.size(); i++){
            ArrayMediciones arrayMediciones = this.arrayMediciones.get(i);
            if(id_room.equals(arrayMediciones.getKeyDispositivo())) {
                if(id_meas == arrayMediciones.getTipoMedicion()){
                    if(id_graph == Constants.GRAFICAR_HABITACION){
                        if(id_meas == Constants.TEMPERATURA_AMBIENTE || id_meas == Constants.CO2)
                            series.get(i).appendData(new DataPoint(m.getDate(), m.getValue()), true, 500);
                        else {
                            series.get(i).appendData(new DataPoint(m.getSample(), m.getValue()), true, 500);
                            UpdateThreshold(i, id_meas, m);
                        }
                    } else {
                        series.get(i).appendData(new DataPoint(m.getDate(), m.getValue()), true, 500);
                        if(id_meas != Constants.TEMPERATURA_AMBIENTE && id_meas != Constants.CO2)
                            UpdateThreshold(i, id_meas, m);
                    }
                }
            }
        }
    }

    public int GetIdGraph(){ return id_graph; }
}