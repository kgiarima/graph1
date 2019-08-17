package com.example.graphtest;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static Random r;
    private static int anxietyLvl;

    private static List<Integer> anxietyTotal, gsrTotal, hrTotal, hrvTotal, sktTotal;
    private static Button startBtn, btBtn, gsrDot, sktDot, hrDot, hrvDot;
    private static boolean isRunning, deviceFound, isConnected;
    private static AnyChartView anyChartView;
    private static CircularGauge circularGauge;
    private static TextView gsrText, sktText, hrText, hrvText;

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
        createChart();
    }

    private void initialize() {

        isRunning = false;
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

        startBtn = (Button) findViewById(R.id.startBtn);
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

    public void btCheck(View view) {

        if (isConnected) {
            try {
                mInputStream.close();
                mSocket.close();

                isRunning = false;
                isConnected = false;
                Toast.makeText(MainActivity.this, "Connection was closed!", Toast.LENGTH_SHORT).show();
                startBtn.setText("Start");
                btBtn.setBackgroundResource(R.drawable.bluetooth_icon_off);
                gsrDot.setBackgroundResource(R.drawable.dot_red);
                sktDot.setBackgroundResource(R.drawable.dot_red);
                hrDot.setBackgroundResource(R.drawable.dot_red);
                hrvDot.setBackgroundResource(R.drawable.dot_red);
            } catch (IOException ex) {
                Toast.makeText(MainActivity.this, "Error while closing the connection", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (bluetoothAdapter.isEnabled()) {

                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice bt : pairedDevices) {
                    if (bt.getName().equals("RN42-CD05")) {
                        mDevice = bt;
                        deviceFound = true;
                        break;
                    }
                    Toast.makeText(MainActivity.this, "Arduino device is not paired with the phone", Toast.LENGTH_SHORT).show();
                }

                //device is paired with the phone
                if (deviceFound) {
                    //try to establish a connection
                    boolean con = connect();

                    if(!con) {
                        Toast.makeText(MainActivity.this, "Device could not connect!", Toast.LENGTH_SHORT).show();
                    }else {
                        isConnected = true;
                        Toast.makeText(MainActivity.this, "Device connected!", Toast.LENGTH_SHORT).show();

                        btBtn.setBackgroundResource(R.drawable.bluetooth_icon);
                        gsrDot.setBackgroundResource(R.drawable.dot_green);
                        sktDot.setBackgroundResource(R.drawable.dot_green);
                        hrDot.setBackgroundResource(R.drawable.dot_green);
                        hrvDot.setBackgroundResource(R.drawable.dot_green);
                    }
                }
            } else {
                Toast.makeText(MainActivity.this, "Bluetooth is off", Toast.LENGTH_SHORT);
            }
        }
    }

    public boolean connect() {
        try {
            // Orismos UUID gia seiriaki metafora dedomenwn
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard Serial Port Service ID

            mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect();
            mInputStream = mSocket.getInputStream();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    void beginListenForData() {

        stopWorker = false;
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                        String line = null;
                        while ((line = in.readLine()) != null) {
                            final String tempLine = line;
                            System.out.println("****** 202 data: " + tempLine);
                            String[] data = tempLine.split("\\s*,\\s*");

                            String status = data[0];
                            int statusCheck = checkHealthStatus(status);  //0 = all good , 1 = gsr error , 2 = hr/hrv error , 3 = skt error, 4 = random error (connection etc)

                            if (statusCheck==0) {

                                int gsr = (int) Double.parseDouble(data[2]);
                                int skt = (int) Double.parseDouble(data[7]);
                                int hr = (int) Double.parseDouble(data[8]);
                                int hrv = (int) Double.parseDouble(data[9]);
                                int anxietyLvl = r.nextInt(100);

                                updateValues(gsr,skt,hr,hrv,anxietyLvl);
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

    public void updateValues(int gsr,int skt,int hr,int hrv,int anxietyLvl) {

        gsrTotal.add(gsr);
        sktTotal.add(skt);
        hrTotal.add(hr);
        hrvTotal.add(hrv);
        anxietyTotal.add(anxietyLvl);

        gsrText.setText("GSR : " + gsr);
        sktText.setText("SKT : " + skt);
        hrText.setText("HR : " + hr);
        hrvText.setText("HRV : " + hrv);

        circularGauge.autoRedraw();//***check
        circularGauge.label(1)
                .text("<span style=\"font-size: 20\">" + anxietyLvl + "</span>") //currentValue should be Anxiety Level
                .useHtml(true)
                .hAlign(HAlign.CENTER);

        circularGauge.data(new SingleValueDataSet(new Double[]{(double) anxietyLvl}));
        circularGauge.autoRedraw();//***check
    }

    public void start(View view) {
        //if procedure has not already started and bluetooth is on..
        if (!isRunning && isConnected) {

            isRunning = true;
            startBtn.setText("Stop");
            beginListenForData();

        } else if (!isRunning && !isConnected) {
            Toast.makeText(MainActivity.this, "Device is not connected", Toast.LENGTH_SHORT).show();
        } else {
            startBtn.setText("Start");
            stopWorker = true;
            isRunning = false;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}

            startActivity(new Intent(MainActivity.this, ResultsActivity.class));
        }
    }

    public int checkHealthStatus(String status) {
        gsrDot.setBackgroundResource(R.drawable.dot_green);
        sktDot.setBackgroundResource(R.drawable.dot_green);
        hrDot.setBackgroundResource(R.drawable.dot_green);
        hrvDot.setBackgroundResource(R.drawable.dot_green);

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
            gsrDot.setBackgroundResource(R.drawable.dot_red);
            sktDot.setBackgroundResource(R.drawable.dot_red);
            hrDot.setBackgroundResource(R.drawable.dot_red);
            hrvDot.setBackgroundResource(R.drawable.dot_red);
            return 4;
        }
    }

    public void createChart() {

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
                .text("<span style=\"font-size: 20\">" + anxietyLvl + "</span>") //currentValue is current Anxiety Level
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

    public static List<Integer> getTotalScore() {
        return anxietyTotal;
    }

    public static List<Integer> getTotalGsr() {
        return gsrTotal;
    }

    public static List<Integer> getTotalSkt() {
        return sktTotal;
    }

    public static List<Integer> getTotalHr() {
        return hrTotal;
    }

    public static List<Integer> getTotalHrv() {
        return hrvTotal;
    }

    public static double getGsr() {
        int sum = 0;
        for (int i = 0; i < gsrTotal.size(); i++) {
            sum += gsrTotal.get(i);
        }
        return sum / gsrTotal.size();
    }

    public static double getSkt() {
        int sum = 0;
        for (int i = 0; i < sktTotal.size(); i++) {
            sum += sktTotal.get(i);
        }
        return sum / sktTotal.size();
    }

    public static double getHr() {
        int sum = 0;
        for (int i = 0; i < hrTotal.size(); i++) {
            sum += hrTotal.get(i);
        }
        return sum / hrTotal.size();
    }

    public static double getHrv() {
        int sum = 0;
        for (int i = 0; i < hrvTotal.size(); i++) {
            sum += hrvTotal.get(i);
        }
        return sum / hrvTotal.size();
    }

}

