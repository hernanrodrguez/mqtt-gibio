package com.example.mqttandroid;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
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
    private ArrayList<Double> data;
    private ArrayList<MeasList> measLists;

    private TextView tvGraphTitle;
    private GridLabelRenderer gridLabel;
    private Viewport viewport;

    private LineGraphSeries<DataPoint> currentSeries;
    private ArrayList<LineGraphSeries<DataPoint>> graphSeries;

    /*
    //add PointsGraphSeries of DataPoint type
    PointsGraphSeries<DataPoint> xySeries;
    private Button btnAddPt;
    private EditText mX,mY;
    GraphView mScatterPlot;
    //make xyPointsArray global
    private ArrayList<XYpoints> xyPointsArray;
    */

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
            //data = (ArrayList<Double>) bundle.getSerializable(Constants.DATA_KEY);

            id_graph = bundle.getInt(Constants.CASE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plot, container, false);
        SetUpGraphView(view);
        return view;

        /*
        btnAddPt = (Button) view.findViewById(R.id.btnAddPt);
        mX = (EditText) view.findViewById(R.id.numX);
        mY = (EditText) view.findViewById(R.id.numY);
        mScatterPlot = (GraphView) view.findViewById(R.id.scatterPlot);
        xyPointsArray = new ArrayList<>();
        init();
        */

        /*
        MeasurementAdapter db = new MeasurementAdapter(getContext());
        db.OpenDB();
        Cursor allRows = db.GetValues("temp_obj");
        allRows.moveToFirst();
        // seguir viendo el PDF del curso de Java 
        */
    }

    private void SetUpGraphView(View v) {
        // https://github.com/jjoe64/GraphView/wiki/Documentation


        //graph = (GraphView) v.findViewById(R.id.graph);

        //for(int i=0; i<data.size(); i++){
        //series.appendData(new DataPoint(i, data.get(i)), true, 20);
        //}

        ScrollView sv = v.findViewById(R.id.scrollView);

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        for(MeasList measList : measLists){
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
            tv.setText(measList.GetRoom().toUpperCase());

            GraphView graph = new GraphView(getContext());
            graph.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            700
                    )
            );
            currentSeries = new LineGraphSeries<>();
            gridLabel = graph.getGridLabelRenderer();
            viewport = graph.getViewport();

            ArrayList<Measurement> arr = measList.GetList();
            for (int i=0; i < arr.size(); i++){
                Measurement measurement = measList.GetList().get(i);
                currentSeries.appendData(new DataPoint(measurement.GetSample(), measurement.GetValue()), true, 20);
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
    public void DataArrived(Double value, int key) {
        //if(key == id_graph)
            //series.appendData(new DataPoint(data.size(), value), true, 20);
    }

    @Override
    public void MeasArrived(String id_room, int id_meas, Measurement measurement) {
        for(int i=0; i<measLists.size();i++){
            MeasList measList = measLists.get(i);
            if(id_room.equals(measList.GetRoom())){
                if(id_meas == id_graph){
                    graphSeries.get(i).appendData(new DataPoint(measurement.GetSample(), measurement.GetValue()), true, 20);
                }
            }
        }

    }

/*
    private void init(){
        //declare the xySeries Object
        xySeries = new PointsGraphSeries<>();

        btnAddPt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mX.getText().toString().equals("") && !mY.getText().toString().equals("") ){
                    double x = Double.parseDouble(mX.getText().toString());
                    double y = Double.parseDouble(mY.getText().toString());
                    Log.d(TAG, "onClick: Adding a new point. (x,y): (" + x + "," + y + ")" );
                    xyPointsArray.add(new XYpoints(x,y));
                    init();
                }else {
                    toastMessage("You must fill out both fields!");
                }
            }
        });

        //little bit of exception handling for if there is no data.
        if(xyPointsArray.size() != 0){
            createScatterPlot();
        }else{
            Log.d(TAG, "onCreate: No data to plot.");
        }
    }


    private void createScatterPlot() {
        Log.d(TAG, "createScatterPlot: Creating scatter plot.");

        //sort the array of xy values
        xyPointsArray = sortArray(xyPointsArray);

        //add the data to the series
        for(int i = 0;i <xyPointsArray.size(); i++){
            try{
                double x = xyPointsArray.get(i).getX();
                double y = xyPointsArray.get(i).getY();
                xySeries.appendData(new DataPoint(x,y),true, 1000);
            }catch (IllegalArgumentException e){
                Log.e(TAG, "createScatterPlot: IllegalArgumentException: " + e.getMessage() );
            }
        }

        //set some properties
        xySeries.setShape(PointsGraphSeries.Shape.RECTANGLE);
        xySeries.setColor(Color.BLUE);
        xySeries.setSize(20f);

        //set Scrollable and Scaleable
        mScatterPlot.getViewport().setScalable(true);
        mScatterPlot.getViewport().setScalableY(true);
        mScatterPlot.getViewport().setScrollable(true);
        mScatterPlot.getViewport().setScrollableY(true);

        //set manual x bounds
        mScatterPlot.getViewport().setYAxisBoundsManual(true);
        mScatterPlot.getViewport().setMaxY(150);
        mScatterPlot.getViewport().setMinY(-150);

        //set manual y bounds
        mScatterPlot.getViewport().setXAxisBoundsManual(true);
        mScatterPlot.getViewport().setMaxX(150);
        mScatterPlot.getViewport().setMinX(-150);

        mScatterPlot.addSeries(xySeries);
    }


      Sorts an ArrayList<XYPoints> with respect to the x values.
      @param array
      @return

    private ArrayList<XYpoints> sortArray(ArrayList<XYpoints> array){

        //Sorts the xyPoints in Ascending order to prepare them for the PointsGraphSeries<DataSet>

        int factor = Integer.parseInt(String.valueOf(Math.round(Math.pow(array.size(),2))));
        int m = array.size() - 1;
        int count = 0;
        Log.d(TAG, "sortArray: Sorting the XYArray.");


        while (true) {
            m--;
            if (m <= 0) {
                m = array.size() - 1;
            }
            Log.d(TAG, "sortArray: m = " + m);
            try {
                //print out the y entrys so we know what the order looks like
                //Log.d(TAG, "sortArray: Order:");
                //for(int n = 0;n < array.size();n++){
                //Log.d(TAG, "sortArray: " + array.get(n).getY());
                //}
                double tempY = array.get(m - 1).getY();
                double tempX = array.get(m - 1).getX();
                if (tempX > array.get(m).getX()) {
                    array.get(m - 1).setY(array.get(m).getY());
                    array.get(m).setY(tempY);
                    array.get(m - 1).setX(array.get(m).getX());
                    array.get(m).setX(tempX);
                } else if (tempX == array.get(m).getX()) {
                    count++;
                    Log.d(TAG, "sortArray: count = " + count);
                } else if (array.get(m).getX() > array.get(m - 1).getX()) {
                    count++;
                    Log.d(TAG, "sortArray: count = " + count);
                }
                //break when factorial is done
                if (count == factor) {
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "sortArray: ArrayIndexOutOfBoundsException. Need more than 1 data point to create Plot." +
                        e.getMessage());
                break;
            }
        }
        return array;
    }


      customizable toast
      @param message

    private void toastMessage(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
    */
}