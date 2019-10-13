package com.example.graphtest;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class MainActivity extends AppCompatActivity {

    private static Random r;
    private static int anxietyLvl, gsr, skt, hr, hrv, amp, lat;
    private static List<Integer> anxietyTotal, gsrTotal, hrTotal, hrvTotal, sktTotal;
    private static List<Integer> anxietyTotal2, gsrTotal2, hrTotal2, hrvTotal2, sktTotal2;
    private static Button startBtn, btBtn, binBtn, gsrDot, sktDot, hrDot, hrvDot;
    private static boolean isRunning, deviceFound, isConnected, binauralOn;
    private static AnyChartView anyChartView;
    private static CircularGauge circularGauge;
    private static TextView gsrText, sktText, hrText, hrvText;
    private MediaPlayer mp;

    // weka
    // model attributes
    private Attribute x1 = new Attribute("gsr");
    private Attribute x2 = new Attribute("ampl");
    private Attribute x3 =new Attribute("latency");
    private Attribute x4 =new Attribute("tempe");
    private Attribute x5 =new Attribute("hr");
    private Attribute x6 =new Attribute("hrv");
    private Attribute result = new Attribute("result");


    // class attribute
    private List<String> EmoLabel = new ArrayList<String>() {};
    private Attribute attributeClass = new Attribute("@@class@@", EmoLabel);
    // rest
    private ArrayList<Attribute> attributeList;
    private Instances dataUnpredicted;
    private Classifier cls;

    //arduino
    BluetoothAdapter bluetoothAdapter;
    ArrayList<String> devices;
    BluetoothDevice mDevice;
    BluetoothSocket mSocket;
    InputStream mInputStream;
    Thread workerThread;
    volatile boolean stopWorker;

    BufferedWriter writer;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        initializeWeka();
        createChart();
    }

    private void initializeWeka() {

//        original was <>(2)
        attributeList = new ArrayList<Attribute>() {
            {
                add(x1);
                add(x2);
                add(x3);
                add(x4);
                add(x5);
                add(x6);
                add(result);
                // add(attributeClass);
            }
        };
        // unpredicted data sets (reference to sample structure for new instances)
        dataUnpredicted = new Instances("TestInstances",attributeList, 1);
        // last feature is target variable
        dataUnpredicted.setClassIndex(dataUnpredicted.numAttributes() - 1);
        // import ready trained model
        cls = null;
        try {
            cls = (RandomForest) weka.core.SerializationHelper //(Classifier) instead of RandForest
                    .read(getAssets().open("randomforest_31.model"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Double predict(final int v1, final int v2, final int v3,final int v4,final int v5,final int v6){

        // **** create new instance: this should be set with all values from arduino
        DenseInstance newInstance = new DenseInstance(dataUnpredicted.numAttributes()) {
            {
                setValue(x1, v1);
                setValue(x2, v5);
                setValue(x3, v6);
                setValue(x4, v2);
                setValue(x5, v3);
                setValue(x6, v4);
            }
        };
        // instance to use in prediction
        // DenseInstance newInstance = newDataInstance;
        // reference to dataset
        newInstance.setDataset(dataUnpredicted);

        // predict new sample
        try {
            double result = cls.classifyInstance(newInstance); //newInstance.instance(0)
            System.out.println("*** 146 Index of predicted class label: " + result + ", which corresponds to class: " + EmoLabel.get(new Double(result).intValue()));
            //String prediction = AttributeClass.result((int)value);

//            predict = gp.classifyInstance(isTest.instance(i));
//            System.out.println("outcome "+predict);
//            double[] p = gp.distributionForInstance(isTest.instance(i));
//            System.out.println(Arrays.toString(p));
//            System.out.print("given value: " + isTest.classAttribute().value((int) isTest.instance(i).classValue()));
//            System.out.println("---predicted value: " + isTest.classAttribute().value((int) predict));
//            prediction = isTest.classAttribute().value((int) predict);
            //result = Double.parseDouble(prediction);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initialize() {

        isRunning = false;
        binauralOn = false;
        r = new Random();

        //bluetooth init
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        devices = new ArrayList<String>();
        deviceFound = false;
        isConnected = false;

        anxietyTotal = new ArrayList<Integer>();
        gsrTotal = new ArrayList<Integer>();
        hrTotal = new ArrayList<Integer>();
        hrvTotal = new ArrayList<Integer>();
        sktTotal = new ArrayList<Integer>();
        anxietyTotal2 = new ArrayList<Integer>();
        gsrTotal2 = new ArrayList<Integer>();
        hrTotal2 = new ArrayList<Integer>();
        hrvTotal2 = new ArrayList<Integer>();
        sktTotal2 = new ArrayList<Integer>();

        startBtn = (Button) findViewById(R.id.startBtn);
        btBtn = (Button) findViewById(R.id.btBtn);
        binBtn = (Button) findViewById(R.id.binBtn);

        gsrDot = (Button) findViewById(R.id.gsrDot);
        sktDot = (Button) findViewById(R.id.sktDot);
        hrDot = (Button) findViewById(R.id.hrDot);
        hrvDot = (Button) findViewById(R.id.hrvDot);

        gsrText = (TextView) findViewById(R.id.gsrTextView);
        sktText = (TextView) findViewById(R.id.sktTextView);
        hrText = (TextView) findViewById(R.id.hrTextView);
        hrvText = (TextView) findViewById(R.id.hrvTextView);

        try {
            cls = (RandomForest) weka.core.SerializationHelper.read(getAssets().open("randomforest_31.model"));
        }catch (Exception e){
            System.out.println(e);
        }
        anyChartView = findViewById(R.id.any_chart_view);
    }

    public void btCheck(View view) {

        if (isConnected) {
            try {
                isRunning = false;
                isConnected = false;
                btBtn.setActivated(false);

                mInputStream.close();
                mSocket.close();

                startBtn.setText("Start");
                setDots(false); //make signal dots red
                Toast.makeText(MainActivity.this, "Connection was closed!", Toast.LENGTH_SHORT).show();
            } catch (IOException ex) {
                Toast.makeText(MainActivity.this, "Error while closing the connection", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (bluetoothAdapter.isEnabled()) {

                //check if device is paired with the phone
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice bt : pairedDevices) {
                    if (bt.getName().equals("RN42-CD05")) {
                        mDevice = bt;
                        deviceFound = true;
                        break;
                    }
                    Toast.makeText(MainActivity.this, "Device is not paired with the phone", Toast.LENGTH_SHORT).show();
                }

                //device is paired with the phone, now we should connect to it
                if (deviceFound) {
                    try {
                        // Orismos UUID gia seiriaki metafora dedomenwn
                        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard Serial Port Service ID
                        mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
                        mSocket.connect();
                        mInputStream = mSocket.getInputStream();

                        isConnected = true;
                        Toast.makeText(MainActivity.this, "Device connected!", Toast.LENGTH_SHORT).show();
                        btBtn.setActivated(true);
                        setDots(true);
                    } catch (IOException ex) {
                        Toast.makeText(MainActivity.this, "Device could not connect!", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(MainActivity.this, "Please turn on the Bluetooth first", Toast.LENGTH_SHORT);
            }
        }
    }

    public void start(View view) {

        if (!isConnected) { // if device is not connected show message
            Toast.makeText(MainActivity.this, "Device is not connected", Toast.LENGTH_SHORT).show();
        } else if (!isRunning && isConnected) { // if connected and not running already start listening for data
            isRunning = true;
            startBtn.setText("Stop");
            beginListenForData();
        } else { // if connected and have already been running stop and show the results
            stopWorker = true;
            isRunning = false;
            startBtn.setText("Start");

            if(binauralOn) {
                pauseBinaural();
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}

            startActivity(new Intent(MainActivity.this, ResultsActivity.class));
        }
    }

    void beginListenForData() {

        stopWorker = false;
        workerThread = new Thread(new Runnable() {
            public void run() {

                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        if (stopWorker) {
                            break;
                        }
                        BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                        String line;
                        while ((line = in.readLine()) != null) {
                            if (stopWorker) {
                                break;
                            }
                            final String tempLine = line;
                            // System.out.println("****** 202 data: " + tempLine);
                            String[] data = tempLine.split("\\s*,\\s*");
                            int statusCheck = checkHealthStatus(data[0]);  //0 = all good , 1 = gsr error , 2 = hr/hrv error , 3 = skt error, 4 = random error (connection etc)

                            if (statusCheck == 0) {
                                gsr = (int) Double.parseDouble(data[2]);
                                skt = (int) Double.parseDouble(data[7]);
                                hr = (int) Double.parseDouble(data[8]);
                                hrv = (int) Double.parseDouble(data[9]);
                                amp = (int) Double.parseDouble(data[8]);
                                lat = (int) Double.parseDouble(data[9]);

                                String str = ("*** data samples: gsr-"+gsr+" | skt-"+skt+" | hr-"+hr+" | hrv-"+hrv+" | amp-"+amp+" | lat-"+lat);
                                System.out.println(str);

                                //anxietyLvl = (int) predict(gsr,skt,hr,hrv,amp,lat).intValue();
                                anxietyLvl = 30;

                                updateValues(gsr, skt, hr, hrv, anxietyLvl);
                            } else{
                                showValues(0,0,0,0,0);
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }

    public void updateValues(int gsrN, int sktN, int hrN, int hrvN, int anxietyN) {

        if (binauralOn) {
            gsrTotal2.add(gsrN);
            sktTotal2.add(sktN);
            hrTotal2.add(hrN);
            hrvTotal2.add(hrvN);
            anxietyTotal2.add(anxietyN);
        }else{
            gsrTotal.add(gsrN);
            sktTotal.add(sktN);
            hrTotal.add(hrN);
            hrvTotal.add(hrvN);
            anxietyTotal.add(anxietyN);
        }
        showValues(gsrN,sktN,hrN,hrvN,anxietyN);
    }

    public void showValues(int gsr, int skt, int hr, int hrv, int anxietyLvl){
        gsrText.setText("GSR : " + gsr);
        sktText.setText("SKT : " + skt);
        hrText.setText("HR : " + hr);
        hrvText.setText("HRV : " + hrv);

        circularGauge.autoRedraw();//***check
        circularGauge.label(1)
                .text("<span style=\"font-size: 20\">" + anxietyLvl + "</span>")
                .useHtml(true)
                .hAlign(HAlign.CENTER);

        circularGauge.data(new SingleValueDataSet(new Double[]{(double) anxietyLvl}));
        circularGauge.autoRedraw();//***check
    }

    public void setDots(boolean set){
        if(set){
            gsrDot.setBackgroundResource(R.drawable.dot_green);
            sktDot.setBackgroundResource(R.drawable.dot_green);
            hrDot.setBackgroundResource(R.drawable.dot_green);
            hrvDot.setBackgroundResource(R.drawable.dot_green);
        }else{
            gsrDot.setBackgroundResource(R.drawable.dot_red);
            sktDot.setBackgroundResource(R.drawable.dot_red);
            hrDot.setBackgroundResource(R.drawable.dot_red);
            hrvDot.setBackgroundResource(R.drawable.dot_red);
        }
    }

    public int checkHealthStatus(String status) {
        setDots(true);

        if (status.equals("M")) {
            return 0;
        } else if (status.equals("G")) {
            gsrDot.setBackgroundResource(R.drawable.dot_red);
            return 1;
        } else if (status.equals("H")) {
            hrDot.setBackgroundResource(R.drawable.dot_red);
            hrvDot.setBackgroundResource(R.drawable.dot_red);
            return 2;
        } else if (status.equals("T")) {
            sktDot.setBackgroundResource(R.drawable.dot_red);
            return 3;
        } else {
            setDots(false);
            return 4;
        }
    }

    public void addBinaural(View view) {

        //player.release()
        if (!binauralOn) {
            binauralOn = true;
            binBtn.setActivated(true);
            Toast.makeText(this, "Binaural Mode is On", Toast.LENGTH_SHORT).show();
            if (mp == null) {
                mp = MediaPlayer.create(MainActivity.this, R.raw.alpha);
            }
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    binBtn.setEnabled(false);
                    binauralOn = false;
                    mp.release();
                    mp = null;
                }
            });
            try {
                // TimeUnit.SECONDS.sleep(5);
                mp.start();

            } catch (Exception e) {
                Toast.makeText(this, "Error playing the binaural", Toast.LENGTH_SHORT).show();
                binauralOn = false;
            }
        } else {
            pauseBinaural();
        }
    }

    public void pauseBinaural() {
        if (mp != null) {
            binauralOn = false;
            binBtn.setActivated(false);
            mp.pause();
            Toast.makeText(this, "Binaural Mode is Off", Toast.LENGTH_SHORT).show();
        }
    }

    public void createChart() {

        circularGauge = AnyChart.circular();

        circularGauge.fill("#fff")
                .stroke(null)
                .padding(0, 0, 0, 0)
                .margin(30, 30, 30, 30);
        circularGauge.startAngle(0)
                .sweepAngle(360);

        anxietyLvl = 0;

        circularGauge.data(new SingleValueDataSet(new Double[]{(double) anxietyLvl}));

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
                .text("<span style=\"font-size: 20\">" + anxietyLvl + "</span>")
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

    public List<Integer> getTotal(String s, int b) {
        if(b==0) {
            if (s.equals("anxiety")) return anxietyTotal;
            else if (s.equals("gsr")) return gsrTotal;
            else if (s.equals("skt")) return sktTotal;
            else if (s.equals("hr")) return hrvTotal;
            else if (s.equals("hrv")) return hrvTotal;
            return anxietyTotal;
        }else{
            if (s.equals("anxiety")) return anxietyTotal2;
            else if (s.equals("gsr")) return gsrTotal2;
            else if (s.equals("skt")) return sktTotal2;
            else if (s.equals("hr")) return hrvTotal2;
            else if (s.equals("hrv")) return hrvTotal2;
            return anxietyTotal2;
        }
    }

    public Map<Integer, Double> getAvg(String s) {
        List<Integer> set1, set2;
        Map<Integer,Double> map = new HashMap<Integer,Double>();
        int sum1 = 0;
        int sum2 = 0;

        if (s.equals("anxiety")) {
            set1 = anxietyTotal;
            set2 = anxietyTotal2;
        } else if (s.equals("gsr")) {
            set1 = gsrTotal;
            set2 = gsrTotal2;
        } else if (s.equals("skt")) {
            set1 = sktTotal;
            set2 = sktTotal2;
        } else if (s.equals("hr")) {
            set1 = hrTotal;
            set2 = hrTotal2;
        } else if (s.equals("hrv")) {
            set1 = hrvTotal;
            set2 = hrvTotal2;
        } else{
            set1 = anxietyTotal;
            set2 = anxietyTotal2;
        }

        for (int x : set1) {
            sum1 += x;
        }

        for (int x : set2) {
            sum2 += x;
        }

        if (set1.size() > 0) {
            double mo = sum1/set1.size();
            map.put(0,mo);
        } else {
            map.put(0,0.0);
        }

        if (set2.size() > 0) {
            double mo = sum2/set2.size();
            map.put(1,mo);
        } else {
            map.put(1,0.0);
        }

        return map;
    }
}

