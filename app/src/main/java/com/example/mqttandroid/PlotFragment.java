package com.example.mqttandroid;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlotFragment#newInstance} factory method to
 * create an instance of this fragment.
 */



public class PlotFragment extends Fragment implements IComData{

    private TextView tvGraphTitle;

    private int id_graph;
    private ArrayList<Double> data;

    private static final String DATA_KEY = "Data";
    private final static String CASE_KEY = "Case";

    private final static int TEMP_OBJ = 1;
    private final static int TEMP_AMB = 2;
    private final static int CO2 = 3;
    private final static int SPO2 = 4;

    private static final String TAG = "PlotFragment";

    private GraphView graph;
    private LineGraphSeries<DataPoint> series;
    private GridLabelRenderer gridLabel;
    private Viewport viewport;

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

    public static PlotFragment newInstance(String param1) {
        PlotFragment fragment = new PlotFragment();
        Bundle args = new Bundle();
        args.putString(DATA_KEY, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            data = (ArrayList<Double>) bundle.getSerializable(DATA_KEY);
            id_graph = bundle.getInt(CASE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plot, container, false);

        /*
        btnAddPt = (Button) view.findViewById(R.id.btnAddPt);
        mX = (EditText) view.findViewById(R.id.numX);
        mY = (EditText) view.findViewById(R.id.numY);
        mScatterPlot = (GraphView) view.findViewById(R.id.scatterPlot);
        xyPointsArray = new ArrayList<>();
        */

        /*
        MeasurementAdapter db = new MeasurementAdapter(getContext());
        db.OpenDB();
        Cursor allRows = db.GetValues("temp_obj");
        allRows.moveToFirst();
        // seguir viendo el PDF del curso de Java 
        */
        SetUpGraphView(view);

        //init();

        return view;
    }

    private void SetUpGraphView(View v){
        // https://github.com/jjoe64/GraphView/wiki/Documentation

        graph = (GraphView) v.findViewById(R.id.graph);
        series = new LineGraphSeries<>();
        gridLabel = graph.getGridLabelRenderer();
        viewport = graph.getViewport();

        for(int i=0; i<data.size(); i++){
            series.appendData(new DataPoint(i, data.get(i)), true, 20);
        }

        graph.addSeries(series);
        viewport.setScrollable(true);
        tvGraphTitle = v.findViewById(R.id.tvGraphTitle);

        switch (id_graph){
            case TEMP_OBJ:
                CustomObjGraph();
                break;
            case TEMP_AMB:
                CustomAmbGraph();
                break;
            case CO2:
                CustomCO2Graph();
                break;
            case SPO2:
                CustomSPO2Graph();
                break;
            default:
                tvGraphTitle.setText("Graficos en desarrollo...");
                break;
        }
    }

    private void CustomSPO2Graph() {
        series.setColor(Color.GREEN);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setThickness(1);
        gridLabel.setHorizontalAxisTitle(getString(R.string.lbl_axis_samples));
        tvGraphTitle.setText(R.string.lbl_graph_spo2);
    }

    private void CustomCO2Graph() {
        series.setColor(Color.DKGRAY);
        gridLabel.setHorizontalAxisTitle(getString(R.string.lbl_axis_time));
        tvGraphTitle.setText(R.string.lbl_graph_co2);
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(400);
    }

    private void CustomAmbGraph() {
        series.setColor(Color.BLUE);
        gridLabel.setHorizontalAxisTitle(getString(R.string.lbl_axis_time));
        gridLabel.setVerticalAxisTitle(getString(R.string.lbl_axis_temp));
        tvGraphTitle.setText(R.string.lbl_graph_amb);
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(40);
    }

    private void CustomObjGraph() {
        series.setColor(Color.RED);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(6);
        series.setThickness(1);
        gridLabel.setHorizontalAxisTitle(getString(R.string.lbl_axis_samples));
        gridLabel.setVerticalAxisTitle(getString(R.string.lbl_axis_temp));
        tvGraphTitle.setText(R.string.lbl_graph_obj);
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(20);
        viewport.setMaxY(40);
    }

    @Override
    public void DataArrived(Double value, int key) {
        if(key == id_graph)
            series.appendData(new DataPoint(data.size(), value), true, 20);
        //graph.addSeries(series);
        //data.add(value);
        //Log.println(Log.DEBUG, "Data Arrived", String.valueOf(data));
        //Toast.makeText(getContext(), "DataArrived", Toast.LENGTH_SHORT).show();
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