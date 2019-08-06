package com.example.graphtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.SingleValueDataSet;
import com.anychart.charts.CircularGauge;
import com.anychart.enums.Anchor;
import com.anychart.graphics.vector.text.HAlign;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static Random r;
    private static int anxietyLvl, gsr, skt, hr, hrv, linesNum;
    private static List<Integer> nums,gsrTotal,hrTotal,hrvTotal,sktTotal, gsrAll, sktAll, hrAll, hrvAll;
    private static Button startBtn, backBtn, btBtn, gsrDot, sktDot, hrDot, hrvDot;
    private static boolean isRunning, isBtOn;
    private static AnyChartView anyChartView;
    private static CircularGauge circularGauge;
    private static double currentValue;
    private static TextView gsrText, sktText, hrText, hrvText;
    private static int btDuration, counter;
    private static Context context;
    private static String btText, text;
    private static Toast btToast;
    private static ArrayList<String> statusAll;
    private static BufferedReader reader;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        setTextValues();
        createBtToast();
        createChart();
    }

    public void start(View view) {

        //if procedure has not already started and bluetooth is on..
        if (!isRunning && isBtOn) {

            isRunning = true;
            startBtn.setText("Stop");

            final Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {

                    // check if procedure has been stopped
                    if (!isRunning) {
                        t.cancel();
                    }

                    anxietyLvl = r.nextInt(100);
//                    gsr = r.nextInt(100);
//                    skt = r.nextInt(100);
//                    hr = r.nextInt(100);
//                    hrv = r.nextInt(100);

                    gsr = gsrAll.get(counter);
                    skt = sktAll.get(counter);
                    hr = hrAll.get(counter);
                    hrv = hrvAll.get(counter);

                    nums.add(anxietyLvl);
                    gsrTotal.add(gsr);
                    sktTotal.add(skt);
                    hrTotal.add(hr);
                    hrvTotal.add(hrv);
                    counter++;

                    currentValue = anxietyLvl;

                    circularGauge.label(1)
                            .text("<span style=\"font-size: 20\">" + currentValue + "</span>") //currentValue should be Anxiety Level
                            .useHtml(true)
                            .hAlign(HAlign.CENTER);

                    circularGauge.data(new SingleValueDataSet(new Double[]{currentValue}));

                    gsrText.setText("GSR : " + gsr);
                    sktText.setText("SKT : " + skt);
                    hrText.setText("HR : " + hr);
                    hrvText.setText("HRV : " + hrv);

                }
            }, 0, 1000);
        }else if(!isRunning && !isBtOn){
            btToast.show();
        }else{
            isRunning = false;
            startBtn.setText("Start");
            startActivity(new Intent(MainActivity.this, ResultsActivity.class));
        }
    }

    public void btCheck(View view){

        if(isBtOn){
            btBtn.setBackgroundResource(R.drawable.bluetooth_icon_off);
            isBtOn =false;
            isRunning = false;
            startBtn.setText("Start");
            gsrDot.setBackgroundResource(R.drawable.dot_red);
            sktDot.setBackgroundResource(R.drawable.dot_red);
            hrDot.setBackgroundResource(R.drawable.dot_red);
            hrvDot.setBackgroundResource(R.drawable.dot_red);
        }else{
            btBtn.setBackgroundResource(R.drawable.bluetooth_icon);
            isBtOn =true;
            gsrDot.setBackgroundResource(R.drawable.dot_green);
            sktDot.setBackgroundResource(R.drawable.dot_green);
            hrDot.setBackgroundResource(R.drawable.dot_green);
            hrvDot.setBackgroundResource(R.drawable.dot_green);
        }

    }

    public static List<Integer> getTotalScore(){
        return nums;
    }

//    public static double getAvg(String val){
//        int sum=0;
//        avg = new ArrayList<Integer>();
//        switch (val) {
//            case "gsr":
//                avg = new ArrayList<Integer>(gsrTotal);
//            case "skt":
//                avg =new ArrayList<Integer>(sktTotal);
//            case "hr":
//                avg =new ArrayList<Integer>(hrTotal);
//            case "hrv":
//                avg =new ArrayList<Integer>(hrvTotal);
//            default:
//                System.out.println("error");
//        }
//        for(int i=0;i<avg.size();i++){
//            sum+= avg.get(i);
//        }
//        return sum/counter;
//    }


    private void initialize() {

        gsrAll = new ArrayList<Integer>();
        sktAll = new ArrayList<Integer>();
        hrAll = new ArrayList<Integer>();
        hrvAll = new ArrayList<Integer>();
        statusAll = new ArrayList<String>();
        isBtOn = false;
        isRunning = false;
        r = new Random();
        counter = 0;
        linesNum = 0;

        nums = new ArrayList<Integer>();
        gsrTotal = new ArrayList<Integer>();
        hrTotal = new ArrayList<Integer>();
        hrvTotal = new ArrayList<Integer>();
        sktTotal = new ArrayList<Integer>();

        startBtn = (Button) findViewById(R.id.startBtn);
        backBtn = (Button) findViewById(R.id.backBtn);
        btBtn = (Button) findViewById(R.id.btBtn);
        gsrDot = (Button) findViewById(R.id.gsrDot);
        sktDot = (Button) findViewById(R.id.sktDot);
        hrDot = (Button) findViewById(R.id.hrDot);
        hrvDot = (Button) findViewById(R.id.hrvDot);

        gsrText = (TextView) findViewById(R.id.gsrTextView);
        sktText = (TextView) findViewById(R.id.sktTextView);
        hrText = (TextView) findViewById(R.id.hrTextView);
        hrvText = (TextView) findViewById(R.id.hrvTextView);

        anyChartView = findViewById(R.id.any_chart_view);
        circularGauge = AnyChart.circular();


    }

    public void setTextValues(){
        try{
            final InputStream file = getAssets().open("demoValues.txt");
            reader = new BufferedReader(new InputStreamReader(file));
            String line = reader.readLine();
            while(line != null){
                linesNum++;
                String[] data = line.split("\\s*,\\s*");
                statusAll.add(data[0]);
                gsrAll.add(Integer.parseInt(data[2]));
                sktAll.add(Integer.parseInt(data[7]));
                hrAll.add(Integer.parseInt(data[8]));
                hrvAll.add(Integer.parseInt(data[9]));
                line = reader.readLine();
            }
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    public void createChart(){

        circularGauge.fill("#fff")
                .stroke(null)
                .padding(0, 0, 0, 0)
                .margin(30, 30, 30, 30);
        circularGauge.startAngle(0)
                .sweepAngle(360);

        currentValue = 0;

        circularGauge.data(new SingleValueDataSet(new Double[] { currentValue }));

        circularGauge.axis(0)
                .startAngle(-150)
                .radius(80)
                .sweepAngle(300)
                .width(3)
                .ticks("{ type: 'line', length: 4, position: 'outside' }");

        circularGauge.axis(0).labels().position("outside");

        circularGauge.axis(0).scale()
                .minimum(0)
                .maximum(100);

        circularGauge.axis(0).scale()
                .ticks("{interval: 10}")
                .minorTicks("{interval: 10}");

        circularGauge.needle(0)
                .stroke(null)
                .startRadius("6%")
                .endRadius("38%")
                .startWidth("2%")
                .endWidth(0);

        circularGauge.cap()
                .radius("4%")
                .enabled(true)
                .stroke(null);

        circularGauge.label(0)
                .text("<span style=\"font-size: 25\">Anxiety Level</span>")
                .useHtml(true)
                .hAlign(HAlign.CENTER);

        circularGauge.label(0)
                .anchor(Anchor.CENTER_TOP)
                .offsetY(100)
                .padding(15, 20, 0, 0);

        circularGauge.label(1)
                .text("<span style=\"font-size: 20\">" + currentValue + "</span>") //currentValue is current Anxiety Level
                .useHtml(true)
                .hAlign(HAlign.CENTER);

        circularGauge.label(1)
                .anchor(Anchor.CENTER_TOP)
                .offsetY(-100)
                .padding(5, 10, 0, 0)
                .background("{fill: 'none', stroke: '#c1c1c1', corners: 3, cornerType: 'ROUND'}");

        circularGauge.range(0,
                "{\n" +
                        "    from: 0,\n" +
                        "    to: 40,\n" +
                        "    position: 'inside',\n" +
                        "    fill: 'green 0.5',\n" +
                        "    stroke: '1 #000',\n" +
                        "    startSize: 6,\n" +
                        "    endSize: 6,\n" +
                        "    radius: 80,\n" +
                        "    zIndex: 1\n" +
                        "  }");

        circularGauge.range(1,
                "{\n" +
                        "    from: 40,\n" +
                        "    to: 75,\n" +
                        "    position: 'inside',\n" +
                        "    fill: 'yellow 0.5',\n" +
                        "    stroke: '1 #000',\n" +
                        "    startSize: 6,\n" +
                        "    endSize: 6,\n" +
                        "    radius: 80,\n" +
                        "    zIndex: 1\n" +
                        "  }");

        circularGauge.range(2,
                "{\n" +
                        "    from: 75,\n" +
                        "    to: 100,\n" +
                        "    position: 'inside',\n" +
                        "    fill: 'red 0.5',\n" +
                        "    stroke: '1 #000',\n" +
                        "    startSize: 6,\n" +
                        "    endSize: 6,\n" +
                        "    radius: 80,\n" +
                        "    zIndex: 1\n" +
                        "  }");

        anyChartView.setChart(circularGauge);

    }

    /*
    initialize a toast to appear when bluetooth connection is off and start is clicked
    the toast will only appear after the command btToast.show() is run
     */
    public void createBtToast(){

        context = getApplicationContext();
        btText = "Bluetooth is not connected!";
        btDuration = Toast.LENGTH_SHORT;
        btToast = Toast.makeText(context, btText, btDuration);
        btToast.setGravity(Gravity.CENTER, 0, 0);

    }

    public static double getGsr(){
        int sum = 0;
        for(int i=0;i<gsrTotal.size();i++){
            sum+= gsrTotal.get(i);
        }
        return sum/counter;
    }

    public static double getSkt(){
        int sum = 0;
        for(int i=0;i<sktTotal.size();i++){
            sum+= sktTotal.get(i);
        }
        return sum/counter;
    }

    public static double getHr(){
        int sum = 0;
        for(int i=0;i<hrTotal.size();i++){
            sum+= hrTotal.get(i);
        }
        return sum/counter;
    }

    public static double getHrv(){
        int sum = 0;
        for(int i=0;i<hrvTotal.size();i++){
            sum+= hrvTotal.get(i);
        }
        return sum/counter;
    }

}
