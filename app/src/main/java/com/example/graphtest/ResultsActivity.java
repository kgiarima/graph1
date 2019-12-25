package com.example.graphtest;

import android.app.Dialog;
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
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ResultsActivity extends AppCompatActivity {

    private static Map<Integer, Double> gsr, skt, hr, hrv, anxiety;
    private static List<Integer> anxietyAll, gsrAll, sktAll, hrAll, hrvAll;
    private static List<Integer> anxietyAll2, gsrAll2, sktAll2, hrAll2, hrvAll2;
    private static Button backBtn;
    private static AnyChartView anyChartView;
    private static TextView gsrText, sktText, hrText, hrvText, resultTextView, resultsText;
    private static Cartesian cartesian;
    private static MainActivity mainData;
    private static String gsrTxt, sktTxt, hrTxt, hrvTxt, results;
    private static Dialog resultsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        backBtn = (Button) findViewById(R.id.backBtn);
        gsrText = (TextView) findViewById(R.id.gsrTextView);
        sktText = (TextView) findViewById(R.id.sktTextView);
        hrText = (TextView) findViewById(R.id.hrTextView);
        hrvText = (TextView) findViewById(R.id.hrvTextView);
        resultTextView = (TextView) findViewById(R.id.resultTextView);

        mainData = new MainActivity();

        gsr = new HashMap<>(mainData.getAvg("gsr")); //Avg("gsr");
        skt = new HashMap<>(mainData.getAvg("skt")); //Avg("skt");
        hr = new HashMap<>(mainData.getAvg("hr")); //Avg("hr");
        hrv = new HashMap<>(mainData.getAvg("hrv")); //Avg("hrv");
        anxiety = new HashMap<>(mainData.getAvg("anxiety")); //Avg("anxiety");

        anxietyAll = new ArrayList<>(mainData.getTotal("anxietyAll", 0));
        gsrAll = new ArrayList<>(mainData.getTotal("gsr", 0));
        sktAll = new ArrayList<>(mainData.getTotal("skt", 0));
        hrAll = new ArrayList<>(mainData.getTotal("hr", 0));
        hrvAll = new ArrayList<>(mainData.getTotal("hrv", 0));

        anxietyAll2 = new ArrayList<>(mainData.getTotal("anxietyAll", 1));
        gsrAll2 = new ArrayList<>(mainData.getTotal("gsr", 1));
        sktAll2 = new ArrayList<>(mainData.getTotal("skt", 1));
        hrAll2 = new ArrayList<>(mainData.getTotal("hr", 1));
        hrvAll2 = new ArrayList<>(mainData.getTotal("hrv", 1));

        setAvgValues();
        anyChartView = findViewById(R.id.any_chart_view);
        cartesian = AnyChart.line();
        setGraph("anxietyAll");
        //showStatistics();
        //setResultsText();
    }

    private void showStatistics() {

        resultTextView.setText("Your average anxiety level was : " + anxiety.get(0).intValue());

        if (anxiety.get(1) > 0 && anxiety.get(0) > 0) {
            Double difference = anxiety.get(1) / anxiety.get(0);
            if (difference > 1) {
                Toast.makeText(ResultsActivity.this, "Your average anxiety with binaural beats enabled was improved " + difference + " times", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ResultsActivity.this, "Your average anxiety with binaural beats enabled declined " + difference + " times", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setAvgValues() {
        gsrTxt = ("Avg GSR \nNo Binaural : " + gsr.get(0) + "\nAlpha Binaural : " + gsr.get(1));
        sktTxt = ("Avg SKT \nNo Binaural : " + skt.get(0) + "\nAlpha Binaural : " + skt.get(1));
        hrTxt = ("Avg HR \nNo Binaural : " + hr.get(0) + "\nAlpha Binaural : " + hr.get(1));
        hrvTxt = ("Avg HRV \nNo Binaural : " + hrv.get(0) + "\nAlpha Binaural : " + hrv.get(1));
        gsrText.setText(gsrTxt);
        sktText.setText(sktTxt);
        hrText.setText(hrTxt);
        hrvText.setText(hrvTxt);
    }

    public void setGraph(String choice) {

        List<Integer> graph, graph2;
        String title;

        if (choice.equals("anxietyAll")) {
            graph = new ArrayList<>(anxietyAll);
            graph2 = new ArrayList<>(anxietyAll2);
            title = "Anxiety Level";
        } else if (choice.equals("gsr")) {
            graph = new ArrayList<>(gsrAll);
            graph2 = new ArrayList<>(gsrAll2);
            title = "GSR Level";
        } else if (choice.equals("skt")) {
            graph = new ArrayList<>(sktAll);
            graph2 = new ArrayList<>(sktAll2);
            title = "SKT Level";
        } else if (choice.equals("hr")) {
            graph = new ArrayList<>(hrAll);
            graph2 = new ArrayList<>(hrAll2);
            title = "HR Level";
        } else if (choice.equals("hrv")) {
            graph = new ArrayList<>(hrvAll);
            graph2 = new ArrayList<>(hrvAll2);
            title = "HRV Level";
        } else {
            graph = new ArrayList<>(anxietyAll);
            graph2 = new ArrayList<>(anxietyAll2);
            title = "Anxiety Level";
        }

        cartesian.animation(true);

        cartesian.padding(5d, 5d, 5d, 5d);

        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                // TODO ystroke
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title(title + " of the session");

        cartesian.yAxis(0).title(title);
        cartesian.xAxis(0).title("Time (sec)");
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        List<DataEntry> seriesData = new ArrayList<>();

//        if(graph.size()>0) {
//            for (int i = 0; graph.size() > graph2.size() ? i < graph.size() : i < graph2.size(); i++) {
//                seriesData.add(new CustomDataEntry("" + i, graph.get(i) != null ? graph.get(i) : 0, graph2.get(i) != null ? graph2.get(i) : 0));
//            }
//        }else{
//            System.out.println("*** 164 size was 0");
//        }
        for (int i = 0; i < graph.size(); i++) {
            seriesData.add(new CustomDataEntry("" + i, graph.get(i) != null ? graph.get(i) : 0));
        }

        Set set = Set.instantiate();
        set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
//        Mapping series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }");
//        Mapping series3Mapping = set.mapAs("{ x: 'x', value: 'value3' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.name(title + "w/o binaural");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

//        Line series2 = cartesian.line(series2Mapping);
//        series2.name(title+"w binaural");
//        series2.hovered().markers().enabled(true);
//        series2.hovered().markers()
//                .type(MarkerType.CIRCLE)
//                .size(4d);
//        series2.tooltip()
//                .position("right")
//                .anchor(Anchor.LEFT_CENTER)
//                .offsetX(5d)
//                .offsetY(5d);

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
    }

    public class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String x, Number value) {

            super(x, value);
        }
    }

//    public class CustomDataEntry extends ValueDataEntry {
//        CustomDataEntry(String x, Number value, Number value2) {
//
//            super(x, value);
//            setValue("value2", value2);
//        }
//    }

    public void showGsr(View view) {
        anyChartView.clear();
        setGraph("gsr");
    }

    public void showHrv(View view) {
        anyChartView.clear();
        setGraph("hrv");
    }

    public void showSkt(View view) {
        anyChartView.clear();
        setGraph("skt");
    }

    public void showHr(View view) {
        anyChartView.clear();
        setGraph("hr");
    }

    public void setResultsText() {
        resultsText = (TextView) resultsDialog.findViewById(R.id.resultsText);
        if (anxiety.get(1) != 0.0) {
            results = "Your average anxiety level during the session was: " + anxiety.get(0).intValue() + "/n";
            results.concat("Your average anxiety level during alpha binaural beats was: " + anxiety.get(1).intValue() + "/n");
            Double difference = anxiety.get(1) / anxiety.get(0);
            if (difference > 1) {
                results.concat("Overall alpha binaural beats improved your anxiety levels " + difference + " times");
            } else {
                results.concat("Overall alpha binaural beats increased your anxiety levels " + difference + " times");
            }
        } else {
            results = "Your average anxiety level during the session was: " + anxiety.get(0) + "/n";
        }
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
}