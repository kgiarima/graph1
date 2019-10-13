package com.example.graphtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class WekaML extends AppCompatActivity {

    // weka
    // model attributes
    final Attribute x1 = new Attribute("gsr");
    final Attribute x2 = new Attribute("ampl");
    final Attribute x3 = new Attribute("latency");
    final Attribute x4 = new Attribute("tempe");
    final Attribute x5 = new Attribute("hr");
    final Attribute x6 = new Attribute("hrv");
    final Attribute result = new Attribute("result");

    // class attribute
    final List<String> EmoLabel = new ArrayList<String>() {
    };
    final Attribute attributeClass = new Attribute("@@class@@", EmoLabel);
    // rest
    private ArrayList<Attribute> attributeList;
    private Instances dataUnpredicted;
    static Classifier cls;
    private static double res;


    public void onCreate() {
        initializeWeka();
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
        dataUnpredicted = new Instances("TestInstances", attributeList, 1);
        // last feature is target variable
        dataUnpredicted.setClassIndex(dataUnpredicted.numAttributes() - 1);

        try {
            cls = (Classifier) weka.core.SerializationHelper.read(getAssets().open("randomforest_31.model"));
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public Double predict(final int v1, final int v2, final int v3, final int v4, final int v5, final int v6) {

        System.out.println("*** 71 inside predict of weka");

        Thread thr = new Thread(new Runnable() {
            public void run() {
                System.out.println("*** 75 Inside run ");
                // **** create new instance: this should be set with all values from arduino
                DenseInstance newDataInstance = new DenseInstance(dataUnpredicted.numAttributes()) {
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
                DenseInstance newInstance = newDataInstance;
                // reference to dataset
                newInstance.setDataset(dataUnpredicted);

                try {
                    res = cls.classifyInstance(newInstance); //newInstance.instance(0)
                    System.out.println("*** Index of predicted class label: " + res + ", which corresponds to class: " + EmoLabel.get(new Double(res).intValue()));
                    //String prediction = AttributeClass.result((int)value);

//            predict = gp.classifyInstance(isTest.instance(i));
//            System.out.println("outcome "+predict);
//            double[] p = gp.distributionForInstance(isTest.instance(i));
//            System.out.println(Arrays.toString(p));
//            System.out.print("given value: " + isTest.classAttribute().value((int) isTest.instance(i).classValue()));
//            System.out.println("---predicted value: " + isTest.classAttribute().value((int) predict));
//            prediction = isTest.classAttribute().value((int) predict);
                    //result = Double.parseDouble(prediction);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        thr.run();
        thr.interrupt();

        System.out.println("*** 110 res is : "+res);
        return res;
    }

}
