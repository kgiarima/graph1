package com.example.graphtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

    private static Map<Integer, Double> gsr, skt, hr, hrv;
    private static Map<Integer, List<Integer>> anxiety, gsrAll, sktAll, hrAll, hrvAll;
    private static Button backBtn;
    private static AnyChartView anyChartView;
    private static TextView gsrText, sktText, hrText, hrvText;
    private static Cartesian cartesian;
    private static MainActivity mainData;
    private static String gsrTxt, sktTxt, hrTxt, hrvTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        backBtn = (Button) findViewById(R.id.backBtn);
        gsrText = (TextView) findViewById(R.id.gsrTextView);
        sktText = (TextView) findViewById(R.id.sktTextView);
        hrText = (TextView) findViewById(R.id.hrTextView);
        hrvText = (TextView) findViewById(R.id.hrvTextView);

        mainData = new MainActivity();

        gsr = new HashMap<>(mainData.getMO("gsr")); //Avg("gsr");
        skt = new HashMap<>(mainData.getMO("skt")); //Avg("skt");
        hr = new HashMap<>(mainData.getMO("hr")); //Avg("hr");
        hrv = new HashMap<>(mainData.getMO("hrv")); //Avg("hrv");

        anxiety = new HashMap<Integer, List<Integer>>(mainData.getTotal("anxiety"));
        gsrAll = new HashMap<Integer, List<Integer>>(mainData.getTotal("gsr"));
        sktAll = new HashMap<Integer, List<Integer>>(mainData.getTotal("skt"));
        hrAll = new HashMap<Integer, List<Integer>>(mainData.getTotal("hr"));
        hrvAll = new HashMap<Integer, List<Integer>>(mainData.getTotal("hrv"));

        setMoValues();
        anyChartView = findViewById(R.id.any_chart_view);
        cartesian = AnyChart.line();
        setGraph("anxiety");
    }

    private void setMoValues() {
        gsrTxt = ("GSR \nNo Binaural : " + gsr.get(0) + "\nAlpha Binaural : " + gsr.get(1));
        sktTxt = ("SKT \nNo Binaural : " + skt.get(0) + "\nAlpha Binaural : " + skt.get(1));
        hrTxt = ("HR \nNo Binaural : " + hr.get(0) + "\nAlpha Binaural : " + hr.get(1));
        hrvTxt = ("HRV \nNo Binaural : " + hrv.get(0) + "\nAlpha Binaural : " + hrv.get(1));
        gsrText.setText(gsrTxt);
        sktText.setText(sktTxt);
        hrText.setText(hrTxt);
        hrvText.setText(hrvTxt);
    }

    public void setGraph(String choice) {

        Map<Integer, List<Integer>> graph;
        String title = "";

        if (choice.equals("anxiety")) {
            graph = new HashMap<Integer, List<Integer>>(anxiety);
            title = "Anxiety Level";
        } else if (choice.equals("gsr")) {
            graph = new HashMap<Integer, List<Integer>>(gsrAll);
            title = "GSR Level";
        } else if (choice.equals("skt")) {
            graph = new HashMap<Integer, List<Integer>>(sktAll);
            title = "SKT Level";
        } else if (choice.equals("hr")) {
            graph = new HashMap<Integer, List<Integer>>(hrAll);
            title = "HR Level";
        } else if (choice.equals("hrv")) {
            graph = new HashMap<Integer, List<Integer>>(hrvAll);
            title = "HRV Level";
        } else {
            graph = new HashMap<Integer, List<Integer>>(anxiety);
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

        for (int i = 0; i < graph.size(); i++) {
            seriesData.add(new CustomDataEntry("" + i, graph.get(0).get(i)));
            //seriesData.add(new CustomDataEntry(""+i, graph.get(1).get(i)));
        }

        Set set = Set.instantiate();
        set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
//        Mapping series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }");
//        Mapping series3Mapping = set.mapAs("{ x: 'x', value: 'value3' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.name(title);
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
//        series2.name("Whiskey");
//        series2.hovered().markers().enabled(true);
//        series2.hovered().markers()
//                .type(MarkerType.CIRCLE)
//                .size(4d);
//        series2.tooltip()
//                .position("right")
//                .anchor(Anchor.LEFT_CENTER)
//                .offsetX(5d)
//                .offsetY(5d);
//
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

}

