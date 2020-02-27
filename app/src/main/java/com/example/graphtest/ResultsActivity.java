package com.example.graphtest;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.core.utils.OrdinalZoom;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ResultsActivity extends AppCompatActivity {

    private static Map<Integer, Double> gsr, emoState;
    private static List<Integer> emoStateAll, emoStateAll2; // 2 is for binaural data
    private static List<Double> gsrAll, gsrAll2;
    private static Button backBtn, saveDataBtn;
    private static AnyChartView anyChartView;
    private static TextView gsrText, resultsText;
    private static Cartesian cartesian;
    private static MainActivity mainData;
    private static String gsrTxt, results;
    private static Dialog resultsDialog;
    private static DatabaseHelper dbHelper;
    private static LoginActivity la;
    private static final String FNAME = "example.txt";
    private static String binaural;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        backBtn = (Button) findViewById(R.id.backBtn);
        gsrText = (TextView) findViewById(R.id.gsrTextView);
        saveDataBtn = (Button) findViewById(R.id.saveDataBtn);

        mainData = new MainActivity();
        dbHelper = new DatabaseHelper(this);

        gsr = new HashMap<>(mainData.getAvgGsr()); //Avg("gsr");
        emoState = new HashMap<>(mainData.getAvg()); //Avg("emo_state");

        emoStateAll = new ArrayList<>(mainData.getTotal( 0));
        gsrAll = new ArrayList<>(mainData.getTotalGsr(0));

        emoStateAll2 = new ArrayList<>(mainData.getTotal(1));
        gsrAll2 = new ArrayList<>(mainData.getTotalGsr( 1));

        binaural = mainData.getBinaural();

        setAvgValues();
        anyChartView = findViewById(R.id.any_chart_view);
        cartesian = AnyChart.line();
        setGraph(0);
    }

    private void setAvgValues() {
        gsrTxt = ("Avg GSR \nNo Binaural : " + gsr.get(0) + "\n"+binaural+" : " + gsr.get(1));
        gsrText.setText(gsrTxt);
    }

    public void setGraph(int choice) {

        List<Double> graph, graph2;
        List<Integer> graph3, graph4;
        String title;

        graph = new ArrayList<>(gsrAll);
        graph2 = new ArrayList<>(gsrAll2);
        graph3 = new ArrayList<>(emoStateAll);
        graph4 = new ArrayList<>(emoStateAll2);
        title = choice==0? "Emotional State": "GSR Level";

        cartesian.xScroller(true);

        OrdinalZoom xZoom = cartesian.xZoom();
        xZoom.setToPointsCount(6, false, null);
        xZoom.getStartRatio();
        xZoom.getEndRatio();

        cartesian.animation(true);
        cartesian.padding(5d, 5d, 5d, 5d);
        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.title(title + " during the session");
        cartesian.yAxis(0).title(title);
        cartesian.xAxis(0).title("Input");
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        List<DataEntry> seriesData = new ArrayList<>();

        if(choice==0) {
            if (graph3.size() >= graph4.size()) {
                for (int i = 0; i < graph3.size(); i++) {
                    if (i < graph4.size()) {
                        seriesData.add(new CustomDataEntry("" + i, graph3.get(i) != null ? graph3.get(i) : 0, graph4.get(i) != null ? graph4.get(i) : 0));
                    } else {
                        seriesData.add(new CustomDataEntry("" + i, graph3.get(i) != null ? graph3.get(i) : 0, null));
                    }
                }
            } else {
                for (int i = 0; i < graph4.size(); i++) {
                    if (i < graph3.size()) {
                        seriesData.add(new CustomDataEntry("" + i, graph3.get(i) != null ? graph3.get(i) : 0, graph4.get(i) != null ? graph4.get(i) : 0));
                    } else {
                        seriesData.add(new CustomDataEntry("" + i, null, graph4.get(i) != null ? graph4.get(i) : 0));
                    }
                }
            }
        }else{
            if (graph.size() >= graph2.size()) {
                for (int i = 0; i < graph.size(); i++) {
                    if (i < graph2.size()) {
                        seriesData.add(new CustomDataEntry("" + i, graph.get(i) != null ? graph.get(i) : 0, graph2.get(i) != null ? graph2.get(i) : 0));
                    } else {
                        seriesData.add(new CustomDataEntry("" + i, graph.get(i) != null ? graph.get(i) : 0, null));
                    }
                }
            } else {
                for (int i = 0; i < graph2.size(); i++) {
                    if (i < graph.size()) {
                        seriesData.add(new CustomDataEntry("" + i, graph.get(i) != null ? graph.get(i) : 0, graph2.get(i) != null ? graph2.get(i) : 0));
                    } else {
                        seriesData.add(new CustomDataEntry("" + i, null, graph2.get(i) != null ? graph2.get(i) : 0));
                    }
                }
            }
        }

        Set set = Set.instantiate();
        set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
        Mapping series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }");
//        Mapping series3Mapping = set.mapAs("{ x: 'x', value: 'value3' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.name(title + " w/o binaural");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        Line series2 = cartesian.line(series2Mapping);
        series2.name(title + " w "+binaural);
        series2.hovered().markers().enabled(true);
        series2.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series2.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

//        Line series3 = cartesian.line(series3Mapping);
//        series3.name("Tequila");
//        series3.hovered().markers().enabled(true);
//        series3.hovered().markers()
//                .type(MarkerType.CIRCLE)
//                .size(4d);
//        series3.tooltip()
//                .position("right")
//                .anchor(Anchor.LEFT_CENTER)
//                .offsetX(5d)
//                .offsetY(5d);

        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(5d, 5d, 10d, 5d);

        anyChartView.setChart(cartesian);
    }

    public void goBack(View view) {
        finish();
        startActivity(new Intent(ResultsActivity.this, MainActivity.class));
    }

    public void saveData(View view) {
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//        LocalDateTime now = LocalDateTime.now();
//        String[] data = {dtf.format(now), getUser(), gsr.get(0).toString(), gsr.get(1).toString(), emoState.get(0).toString(), emoState.get(1).toString()};
//        boolean insertData = dbHelper.addData(data);
//        if (insertData) {
//            Toast.makeText(this, "Data successfully stored", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "Data could not be stored", Toast.LENGTH_SHORT).show();
//        }
        la = new LoginActivity();

        try{
            FileInputStream fis = openFileInput(FNAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String existingData;
            while((existingData = br.readLine())!=null){
                sb.append(existingData).append("\n");
            }
            if(fis!=null){
                fis.close();
            }

            String textToAdd = "\nUser: "+la.getUserName()+" "+la.getUserSurname()+"\n";
            List<String> data = mainData.getData();
            for(int i=0;i<data.size();i++){
                textToAdd = textToAdd+data.get(i)+"\n";
            }


            FileOutputStream fos = openFileOutput(FNAME, MODE_PRIVATE);
            fos.write((sb.toString().concat(textToAdd)).getBytes());
            if(fos!=null){
                fos.close();
            }
            Toast.makeText(this, "Data successfully stored in "+getFilesDir(), Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            Toast.makeText(this, "Data could not be stored", Toast.LENGTH_SHORT).show();
        }
    }

//    public class CustomDataEntry extends ValueDataEntry {
//        CustomDataEntry(String x, Number value) {
//
//            super(x, value);
//        }
//    }

    public void showGsr(View view) {
        anyChartView.clear();
        setGraph(1);
    }

    public void setResultsText() {
        resultsText = (TextView) resultsDialog.findViewById(R.id.resultsText);
        String emoStateMessage = getResultsMessage((int) Math.round(emoState.get(0)));
        String emoStateMessage2 = getResultsMessage((int) Math.round(emoState.get(1)));
        String message = "Your average emotional state during the measurement was : ";
        message+=emoStateMessage;
        if(emoState.get(1)>=0){
            message+="\n\nYour average emotional state with '"+binaural+"' on was : "+emoStateMessage2;
        }
        resultsText.setText(message);
    }

    public String getResultsMessage(double value){
        String message;
        switch ((int) Math.round(value)) {
            case 0:
                message = "Low Baseline";
                break;
            case 1:
                message = "Medium Baseline";
                break;
            case 2:
                message = "High Baseline";
                break;
            case 3:
                message = "Low Amusement";
                break;
            case 4:
                message = "Medium Amusement";
                break;
            case 5:
                message = "High Amusement";
                break;
            case 6:
                message = "Low Stress";
                break;
            case 7:
                message = "Medium Stress";
                break;
            case 8:
                message = "High Stress";
                break;
            default:
                message = "Not found";
                break;
        }
        return message;
    }

    public void showResults(View view) {
        resultsDialog = new Dialog(this);
        resultsDialog.setContentView(R.layout.results_message);
        setResultsText();
        Button close = (Button) resultsDialog.findViewById(R.id.closeBtn);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultsDialog.dismiss();
            }
        });
        resultsDialog.show();
    }

    public class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String x, Number value, Number value2) {

            super(x, value);
            setValue("value2", value2);
        }
    }
}