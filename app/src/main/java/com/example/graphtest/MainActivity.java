package com.example.graphtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.function.Min;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import weka.classifiers.trees.RandomForest;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.converters.ConverterUtils.DataSource;

import static org.apache.commons.math3.stat.StatUtils.max;
import static org.apache.commons.math3.stat.StatUtils.mean;
import static org.apache.commons.math3.stat.StatUtils.min;

public class MainActivity extends AppCompatActivity {

    private static int emoState, count, statusCheck;
    private static double meanGsr,maxGsr,minGsr,rangeGsr,kurtGsr,skewGsr,gsr;
    private static double gsrActive[];
    private static List<Integer> emoStateTotal, emoStateTotal2;
    private static List<Double> gsrTotal, gsrTotal2;
    private static Button startBtn, btBtn, binBtn, gsrDot;
    private static boolean isRunning, deviceFound, isConnected, binauralOn;
    private static AnyChartView anyChartView;
    private static CircularGauge circularGauge;
    private static TextView gsrText;
    private MediaPlayer mp;

    private Handler mainHandler = new Handler();

    // weka
    private static RandomForest rf;
    private static DataSource src;
    private static Instances ds;
    private static Instance testInstance;
    private static int emoStatePredict;

    //arduino
    BluetoothAdapter bluetoothAdapter;
    ArrayList<String> devices;
    BluetoothDevice mDevice;
    BluetoothSocket mSocket;
    InputStream mInputStream;
    Thread workerThread;
    volatile boolean stopWorker;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        initializeWeka();
        createChart();
    }

    private void initialize() {

        count = 0;
        isRunning = false;
        binauralOn = false;

        //bluetooth init
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        devices = new ArrayList<String>();
        deviceFound = false;
        isConnected = false;

        gsrActive = new double[50];
        emoStateTotal = new ArrayList<Integer>();
        gsrTotal = new ArrayList<Double>();
        emoStateTotal2 = new ArrayList<Integer>();
        gsrTotal2 = new ArrayList<Double>();

        startBtn = (Button) findViewById(R.id.startBtn);
        btBtn = (Button) findViewById(R.id.btBtn);
        binBtn = (Button) findViewById(R.id.binBtn);

        gsrDot = (Button) findViewById(R.id.gsrDot);
        gsrText = (TextView) findViewById(R.id.gsrTextView);

        anyChartView = findViewById(R.id.any_chart_view);
    }

    private void initializeWeka() {
        try {
            rf = (RandomForest) weka.core.SerializationHelper.read(getAssets().open("gsrToEmotion.model"));
            src = new DataSource(getAssets().open("gsrData.arff"));
            ds = src.getDataSet();
            ds.setClassIndex(ds.numAttributes() - 1);
            testInstance = new DenseInstance(7); // mean, max, min, range, kurt, skew, F_Label
            testInstance.setDataset(ds);
        } catch (Exception e) {
            System.out.println(e);
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

        emoState = -1;

        circularGauge.data(new SingleValueDataSet(new Double[]{(double) emoState}));

        circularGauge.axis(0)
                .startAngle(-150)
                .radius(80)
                .sweepAngle(300)
                .width(3)
                .ticks("{ type: 'line', length: 4, position: 'outside' }");

        circularGauge.axis(0).labels().position("outside");

        circularGauge.axis(0).scale()
                .minimum(-1)
                .maximum(8); //or 100

        circularGauge.axis(0).scale()
                .ticks("{interval: 1}")
                .minorTicks("{interval: 1}");

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
                .text("<span style=\"font-size: 25\">Emotional State</span>")
                .useHtml(true)
                .hAlign(HAlign.CENTER);

        circularGauge.label(0)
                .anchor(Anchor.CENTER_TOP)
                .offsetY(100)
                .padding(15, 20, 0, 0);

        circularGauge.label(1)
                .text("<span style=\"font-size: 20\">" + emoState + "</span>")
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
                        "    to: 2,\n" + //40
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
                        "    from: 3,\n" +
                        "    to: 5,\n" + //75
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
                        "    from: 6,\n" +
                        "    to: 8,\n" + //100
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

    private int predict() {
        try {
            testInstance.setValue(ds.attribute("mean"), meanGsr);
            testInstance.setValue(ds.attribute("max"), maxGsr);
            testInstance.setValue(ds.attribute("min"), minGsr);
            testInstance.setValue(ds.attribute("range"), rangeGsr);
            testInstance.setValue(ds.attribute("kurt"), kurtGsr);
            testInstance.setValue(ds.attribute("skew"), skewGsr);

            System.out.println("*****The instance: " + testInstance);
            emoStatePredict = (int) rf.classifyInstance(testInstance);
            System.out.println(emoStatePredict);

            return emoStatePredict;
        }catch(Exception e){
            System.out.println(e);
        }
        return -1;
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

            if (binauralOn) {
                pauseBinaural();
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println(e);
            }

            finish();
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
                        in.readLine();
                        String line;
                        while ((line = in.readLine()) != null) {
                            if (stopWorker) {
                                break;
                            }
                            final String tempLine = line;
                            String[] data = tempLine.split("\\s*,\\s*");
                            System.out.println("***355 "+tempLine);

                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    gsr = Double.parseDouble(data[1]);
                                    statusCheck = 1;
                                    try {
                                        statusCheck = checkHealthStatus(data[0]);  // 0 = ok , 1 = error
                                    }catch(Exception e){ }
                                    if (statusCheck == 0) {

                                        gsrActive[count%50] = gsr;
                                        count++;

                                        if(count>50 && count%25==0) {
                                            setModelValues();
                                            emoState = predict();
                                            updateValues();
                                            showValues(meanGsr, emoState);
                                        }
                                        if(count%10==0) {
                                            showValues(gsr, emoState);
                                        }

                                    } else {
                                        showValues(0.0,  -1);
                                    }
                                }
                            });


                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }

    public void setModelValues(){

        meanGsr = mean(gsrActive);
        minGsr = min(gsrActive);
        maxGsr = max(gsrActive);
        rangeGsr = maxGsr - minGsr;
        skewGsr = new Skewness().evaluate(gsrActive);
        kurtGsr = new Kurtosis().evaluate(gsrActive);

//        for(int i=0;i<5;i++){
//            gsrActive = ArrayUtils.remove(gsrActive,0);
//        }
    }

    // choice = 0 means that both gsr and emo_state should be added ti the total Lists. If choice = 1 only gsr should be added to the total List
    public void updateValues( ) {

        if (binauralOn) {
            gsrTotal2.add(meanGsr);
            emoStateTotal2.add(emoState);
        } else {
            gsrTotal.add(meanGsr);
            emoStateTotal.add(emoState);
        }
    }

    public void showValues(double gsr,int emoState) {
        gsrText.setText("GSR : " + gsr);
        // +"\n Count : "+count
        System.out.println("** count : "+count);
        circularGauge.autoRedraw();//***check
        circularGauge.label(1)
                .text("<span style=\"font-size: 20\">" + emoState + "</span>")
                .useHtml(true)
                .hAlign(HAlign.CENTER);

        circularGauge.data(new SingleValueDataSet(new Double[]{(double) emoState}));
        circularGauge.autoRedraw();//***check
    }

    public void setDots(boolean set) {
        if (set) {
            gsrDot.setBackgroundResource(R.drawable.dot_green);
        } else {
            gsrDot.setBackgroundResource(R.drawable.dot_red);
        }
    }

    public int checkHealthStatus(String status) {
        setDots(true);

            if (status.equals("M") && gsr>0 && gsr<20) {
                setDots(true);
                return 0;
            } else {
                setDots(false);
                return 1;
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

    public List<Integer> getTotal(int b) {
        if (b == 0) {
             return emoStateTotal;
        } else {
            return emoStateTotal2;
        }
    }

    public List<Double> getTotalGsr(int b) {
        if (b == 0) {
            return gsrTotal;
        } else {
            return gsrTotal2;
        }
    }

    public Map<Integer, Double> getAvg() {
        List<Integer> set1, set2;
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        int sum1 = 0;
        int sum2 = 0;

        set1 = emoStateTotal;
        set2 = emoStateTotal2;

        for (int x : set1) {
            sum1 += x;
        }

        for (int x : set2) {
            sum2 += x;
        }

        if (set1.size() > 0) {
            double mo = sum1 / set1.size();
            map.put(0, mo);
        } else {
            map.put(0, -1.0);
        }

        if (set2.size() > 0) {
            double mo = sum2 / set2.size();
            map.put(1, mo);
        } else {
            map.put(1, -1.0);
        }

        return map;
    }

    public Map<Integer, Double> getAvgGsr() {
        List<Double> set1, set2;
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        int sum1 = 0;
        int sum2 = 0;

        set1 = gsrTotal;
        set2 = gsrTotal2;

        for (double x : set1) {
            sum1 += x;
        }

        for (double x : set2) {
            sum2 += x;
        }

        if (set1.size() > 0) {
            double mo = sum1 / set1.size();
            map.put(0, mo);
        } else {
            map.put(0, 0.0);
        }

        if (set2.size() > 0) {
            double mo = sum2 / set2.size();
            map.put(1, mo);
        } else {
            map.put(1, 0.0);
        }

        return map;
    }
}