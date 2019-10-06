//package com.example.graphtest;
//
//import java.util.Arrays;
//import java.util.Calendar;
//
//import weka.classifiers.trees.RandomForest;
//import weka.core.Attribute;
//import weka.core.FastVector;
//import weka.core.Instances;
//import weka.experiment.InstanceQuery;
//import weka.filters.Filter;
//import weka.filters.unsupervised.attribute.Normalize;
//
//public class Weka {
//    String trainf = "randomforest_" + strUid + ".model";
//    keepMeasurements(iUsr,gsrcurr, gsrValue,gsrPhasic,gsrAmpl, gsrLate,strRise,temperatureV,HeartRate, ivbi);
//
//
//		if (cn == 0) //first time
//    {
//        //exam if user is trained
//        trainned = examUserTrainned(iUsr);
//        if (!trainned) //stop the measurement
//
//
//
//
//    }
//		else
//    {
//        processAnxiety(strUid,gsrcurr,gsrValue,gsrPhasic,gsrAmpl, gsrLate,strRise,temperatureV,HeartRate);
//
//
//    }
//
//    public boolean examUserTrainned(int usr)
//    {
//        boolean trainned = false;
//        String todo = (	"  SELECT  TrainFlag   " +
//                "    FROM  usersTrain      " +
//                "   WHERE  UserID = '"+usr+"' ") ;
//
//        try
//        {
//
//            java.sql.ResultSet r = l.SelRecords(todo);
//
//            while (r.next())
//            {
//
//                int TrainInd = r.getInt("TrainFlag");
//                if (TrainInd == 1) trainned = true;
//
//            }
//
//        }
//        catch (Exception e) {
//            System.out.println("Select Error\n"+e.getMessage());
//        }
//        return trainned;
//
//    }
//
//    public void processAnxiety(String strUid,double currentReading, double gsrValue,  double gsrPhasic,double gsrAmpl, double gsrLate,String strRise,double temperatureVal,double pulseRate)
//    {
//        double corrCoef = 0;
//        double errorRate=0;
//        double MeanSquaredError=0;
//        double MeanAbsError = 0;
//
//
//
//
//        double pred = 0 ;
//        double pred1 = 0;
//        Instances testData = null;
//        double temperatureV;
//        double baseLine;
//        double HeartRate;
//
//        long lastFlatLine=0;
//        int iUsr;
//        Instances isTest = null;
//
//
//
//
//        iUsr= Integer.parseInt(strUid);
//
//        if (gsrValue<=0 ){
//
//
//
//            currentReading=0;//flatline
//            gsrValue=0;
//            gsrAmpl = 0;
//            gsrLate = 0;
//            baseLine=0;
//            lastFlatLine=System.currentTimeMillis();
//
//        }
//
//        if (temperatureVal > 10 & temperatureVal < 40){
//            temperatureV = temperatureVal;
//
//        }
//        else
//        {
//            temperatureV=0;
//
//        }
//
//        if (pulseRate>30 & pulseRate<180){
//            HeartRate=pulseRate;
//
//        }
//        else
//        {
//            HeartRate=0;
//
//        }
//
//        if ((gsrValue > 0)&&(temperatureV>0)&&(HeartRate > 0))
//        {
//
//            double dbgsrvalue = 0;
//            double dbgsrAmpl = 0;
//            double dbgsrLate = 0;
//            double dbgsrPhasic = 0;
//            double dbheartrate = 0;
//            double dbtemperatureval =  0;
//            double dbHRV = 0;
//            double dbEmo = 0;
//            double ivbi = 0;
//            int cntTest = 0;
//
//
//
//            if (ivbi == 0)ivbi = 1;
//            if (gsrAmpl==0) gsrAmpl = 1;
//            if (gsrLate == 0) gsrLate = 1;
//
//
//
//            dbgsrvalue = gsrValue;
//            dbgsrAmpl = gsrAmpl;
//            dbgsrLate = gsrLate;
//            dbgsrPhasic = gsrPhasic;
//            dbtemperatureval = temperatureV ;
//            dbheartrate = HeartRate;
//            dbHRV= ivbi;
//
//            double predict=0;
//            String prediction = "";
//
//            String todoA = ("  SELECT  COUNT(*) AS CNT   " +
//                    "    FROM  dbtest   WHERE UserID = '"+iUsr+"'   " );
//
//            try
//            {
//                java.sql.ResultSet ra = l.SelRecords(todoA);
//
//                while (ra.next())
//                {
//                    cntTest = ra.getInt("CNT");
//
//
//                }
//            }
//            catch (Exception ea) {
//                System.out.println("Select Error\n"+ea.getMessage());
//            }
//
//
//
//
//            dbEmo = dbEmo + 10;
//            if (dbEmo > 50) dbEmo = 10;
//
//            String todo2 = ("INSERT " +
//                    "           INTO    	dbtest " +
//                    "                     (UserID,  " +
//                    "                     GsrTrain, TrainAmpl, TrainLate, TempeTrain, HeartTrain, HRV,    " +
//                    "                     EmoLabel ) " +
//                    "            VALUES ('"+iUsr+"',  " +
//                    "                    '"+dbgsrvalue+"', " +
//                    "                    '"+dbgsrAmpl+"', " +
//                    "                    '"+dbgsrLate+"',  " +
//                    "                    '"+dbtemperatureval+"'," +
//                    "                    '"+dbheartrate+"', '"+dbHRV+"',  '"+dbEmo+"')") ;
//
//            // println (todo2);
//            try {
//
//                l.InsRecords(todo2);
//            }
//            catch (Exception e) {
//                System.out.println("Insert Error\n"+e);
//            }
//
//            if (cntTest >= 20)
//            {
//
//                try {
//                    String trf="C:/temp/"+trainf;
//                    System.out.println(trf);
//                    gp =  (RandomForest) weka.core.SerializationHelper.read(trf);
//
//                } catch (Exception e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }
//
//                testData = getInstance(iUsr);
//
//                Normalize normalize = new Normalize();
//                try {
//                    normalize.setInputFormat(testData);
//                } catch (Exception e2) {
//                    // TODO Auto-generated catch block
//                    e2.printStackTrace();
//                }
//                try {
//                    isTest = Filter.useFilter(testData, normalize);
//                } catch (Exception e2) {
//                    // TODO Auto-generated catch block
//                    e2.printStackTrace();
//                }
//
//                isTest.setClassIndex(isTest.numAttributes()-1);
//
//
//
//
//
//                try {
//                    gp.setNumTrees(50);
//                    //String[] options = new String[2];
//                    //options[0] = "-R";
//
//                    //gp.setOptions(options);
//
//                    //gp.setOptions(weka.core.Utils.splitOptions("-L 1.0 -N 0 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\""));
//                } catch (Exception e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }
//
//
//                System.out.print("Instance Counter" + Instancecnt);
//                //if (Instancecnt >= 20)
//                //{
//
//                try {
//
//                    for(int i=0;i<isTest.numInstances();i++){
//                        predict = gp.classifyInstance(isTest.instance(i));
//                        System.out.println("outcome "+predict);
//                        double[] p = gp.distributionForInstance(isTest.instance(i));
//                        System.out.println(Arrays.toString(p));
//                        System.out.print("given value: " + isTest.classAttribute().value((int) isTest.instance(i).classValue()));
//                        System.out.println("---predicted value: " + isTest.classAttribute().value((int) predict));
//                        prediction = isTest.classAttribute().value((int) predict);
//                    }
//
//
//
//
//
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//
//
//
//
//                pred = Double.parseDouble(prediction);
//                Instancecnt = 0;
//
//
//
//
//                String todo0D = (	" DELETE FROM  dbtest  WHERE UserID = '"+iUsr+"'   ") ;
//
//
//                System.out.println(todo0D);
//                try {
//                    l.DelRecords(todo0D);
//                }
//                catch (Exception e03) {
//                    System.out.println("Delete Error\n"+e03.getMessage());
//                }
//
//                basicEMOcluster(pred);
//                keepAnxiety(pred, errorRate, MeanSquaredError, corrCoef, MeanAbsError,iUsr,gsrValue,gsrPhasic,gsrAmpl, gsrLate,strRise,temperatureV,HeartRate, ivbi);
//
//
//            }
//
//
//
//
//
//        }
//    }
//
//    public static Instances getInstance(int usr) {
//
//
//
//        Instances predictionSet = initializeDataset();
//
//
//
//
//        InstanceQuery query = null;
//        try {
//            query = new InstanceQuery();
//        } catch (Exception e3) {
//            // TODO Auto-generated catch block
//            e3.printStackTrace();
//        }
//        query.setUsername("root");
//        query.setPassword("");
//        query.setDatabaseURL("jdbc:mysql://localhost/emogame?#characterEncoding=UTF-8");
//        query.setQuery("  SELECT  DISTINCT GsrTrain, TrainAmpl, TrainLate, TempeTrain, HeartTrain, HRV, EmoLabel  " +
//                "    FROM  dbtest 	" +
////		   				  "   WHERE  UserID = 32 ") ;
//                "   WHERE  UserID = '"+usr+"' ") ;
//        // You can declare that your data set is sparse
//        query.setSparseData(true);
//
//        try {
//            predictionSet = query.retrieveInstances();
//        } catch (Exception e2) {
//            // TODO Auto-generated catch block
//            e2.printStackTrace();
//        }
//
//
//        Instancecnt++;
//
//        return predictionSet;
//
//    }
//
//    public static Instances initializeDataset(){
//
//        FastVector fvWekaAttributes = new FastVector(3);
//
//
//        Attribute x1 = new Attribute("gsr");
//        Attribute x2 = new Attribute("ampl");
//        Attribute x3 =new Attribute("latency");
//        Attribute x4 =new Attribute("tempe");
//        Attribute x5 =new Attribute("hr");
//        Attribute x6 =new Attribute("hrv");
//        Attribute result = new Attribute("result");
//
//        fvWekaAttributes.addElement(x1);
//        fvWekaAttributes.addElement(x2);
//        fvWekaAttributes.addElement(x3);
//        fvWekaAttributes.addElement(x4);
//        fvWekaAttributes.addElement(x5);
//        fvWekaAttributes.addElement(x6);
//        fvWekaAttributes.addElement(result);
//
//        Instances testset = new Instances("testset",fvWekaAttributes,0);
//
//        return testset;
//
//    }
//
//    public void basicEMOcluster(double pred){
//
//        String ipred = "";
//
//        String cl1 = "1.0";
//        String cl2 = "2.0";
//        String cl3 = "3.0";
//        String cl4 = "4.0";
//        String cl5 = "5.0";
//        int colr = 0;
//        int prevcolr =0;
//
//        if (pred == 10) ipred = "1.0"; //relaxation
//        else if (pred == 20)  ipred = "2.0"; //reduced relaxation
//        else if (pred == 30)  ipred = "3.0"; //neutral
//        else if (pred == 40)  ipred = "4.0"; //Moderate anxiety
//        else if (pred == 50)  ipred = "5.0"; //High anxiety
//
//
//
//        //System.out.println("prediction " + pred);
//
//        //updChart((int)pred);
//
//        if (ipred.equals(cl1)) colr = 1;
//        else if (ipred.equals(cl2)) colr = 2;
//        else if (ipred.equals(cl3)) colr = 3;
//        else if (ipred.equals(cl4)) colr = 4;
//        else if (ipred.equals(cl5)) colr = 5;
//        else colr = 99;
//
//
//
//        if (prevcolr != colr)
//        {
//
//            prevcolr=colr;
//        }
//
//    }
//
//    public void keepAnxiety(double val1, double val2, double val3, double val4, double val5, int Uid, double gsrValue,  double gsrPhasic,double gsrAmpl, double gsrLate,String strRise,double temperatureVal,double pulseRate, double iHRV)
//    {
//
//        double dbSDNN = 0;
//        int	  iEmoLabel = 0;
//        double   iEmoLabelP = 0;
//
//        double dbgsrcurR = 0;
//        double dbgsrvalue = 0;
//        double dbgsrAmpl = 0;
//        double dbgsrLate = 0;
//        double dbgsrPhasic = 0;
//        double dbheartrate = 0;
//        double dbtemperatureval =  0;
//        double dbHRV = 0;
//        Object predictedClassValue = 0;
//
//        double corrCoef = 0;
//        double errorRate=0;
//        double MeanSquaredError=0;
//        double  MeanAbsError = 0;
//
//        double pred = 0;
//        double pred1 = 0;
//
//        int	iGroupID = 0;
//        int	iGengroupID = 0;
//        double aDeg=0;
//
//
//        //dbgsrcurR = gsrcurR;
//        dbgsrvalue = gsrValue;
//        dbgsrAmpl = gsrAmpl;
//        dbgsrLate = gsrLate;
//        dbgsrPhasic = gsrPhasic;
//        dbtemperatureval = temperatureVal ;
//        dbheartrate = pulseRate;
//        dbHRV = iHRV;
//
//        pred = val1;
//        errorRate = val2;
//        MeanSquaredError = val3;
//        corrCoef = val4;
//        MeanAbsError = val5;
//
//
//
//           /*
//	 		if ( ( dbtemperatureval != 0) && ( dbheartrate != 0))
//	 			activities = 1;
//	 		else activities = 0;
//	 		*/
//
//        Calendar cal = Calendar.getInstance();
//        int day = cal.get(Calendar.DAY_OF_MONTH);
//        int month = cal.get(Calendar.MONTH) + 1;
//        int year = cal.get(Calendar.YEAR);
//        String strDDMMYYYY = Integer.toString(day)+ "/" +
//                Integer.toString(month) + "/" +
//                Integer.toString(year);
//        int timehh = cal.get(Calendar.HOUR_OF_DAY);
//        int min = cal.get(Calendar.MINUTE);
//        int sec = cal.get(Calendar.SECOND);
//        String todo2 = ("INSERT " +
//                "           INTO    anxietydatanp " +
//                "                     (UserID, GroupID, " +
//                "                      GengroupID, GsrCur, GsrValue,  " +
//                "                      GsrAmpl, " +
//                "                      GsrLatency, " +
//                "                      DateGsr, TimeGsr," +
//                "                      MinGsr,  SecGsr, Temperature, " +
//                "                      HRate, HRV, EmoLabelPer,  ErrorRate, MeanSquaredError, " +
//                "                      SensorDeg ) " +
//                "            VALUES ('"+Uid+"', '"+iGroupID+"', " +
//                "                    '"+iGengroupID+"','"+dbgsrcurR+"', '"+dbgsrvalue+"', " +
//                "                    '"+dbgsrAmpl+"', " +
//                "                    '"+dbgsrLate+"',  " +
//                "                    '"+strDDMMYYYY+"', '"+timehh+"', " +
//                "                    '"+min+"', '"+sec+"', '"+dbtemperatureval+"'," +
//                "                    '"+dbheartrate+"', '"+dbHRV+"', '"+pred+"',  '"+errorRate+"', '"+MeanSquaredError+"', " +
//                "                    '"+aDeg+"')") ;
//        //        "                    '"+min+"', '"+sec+"', '"+strDact+"' )") ;
//        //println (todo2);
//        try {
//
//            l.InsRecords(todo2);
//        }
//        catch (Exception e) {
//            System.out.println("Insert Error\n"+e);
//        }
//
//
//
//    }
//
//
//    public void keepMeasurements(int Uid, double gsrValue, double gsrcurR,  double gsrPhasic,double gsrAmpl, double gsrLate,String strRise,double temperatureVal,double pulseRate, double iHRV)
//    {
//
//        double dbSDNN = 0;
//        int	  iEmoLabel = 0;
//        double   iEmoLabelP = 0;
//
//        double dbgsrcurR = 0;
//        double dbgsrvalue = 0;
//        double dbgsrAmpl = 0;
//        double dbgsrLate = 0;
//        double dbgsrPhasic = 0;
//        double dbheartrate = 0;
//        double dbtemperatureval =  0;
//        double dbHRV = 0;
//        Object predictedClassValue = 0;
//
//        double corrCoef = 0;
//        double errorRate=0;
//        double MeanSquaredError=0;
//        double  MeanAbsError = 0;
//
//        double pred = 0;
//        double pred1 = 0;
//
//        int	iGroupID = 0;
//        int	iGengroupID = 0;
//        double aDeg=0;
//
//
//        dbgsrcurR = gsrcurR;
//        dbgsrvalue = gsrValue;
//        dbgsrAmpl = gsrAmpl;
//        dbgsrLate = gsrLate;
//        dbgsrPhasic = gsrPhasic;
//        dbtemperatureval = temperatureVal ;
//        dbheartrate = pulseRate;
//        dbHRV = iHRV;
//
//
//
//
//
//           /*
//	 		if ( ( dbtemperatureval != 0) && ( dbheartrate != 0))
//	 			activities = 1;
//	 		else activities = 0;
//	 		*/
//
//        Calendar cal = Calendar.getInstance();
//        int day = cal.get(Calendar.DAY_OF_MONTH);
//        int month = cal.get(Calendar.MONTH) + 1;
//        int year = cal.get(Calendar.YEAR);
//        String strDDMMYYYY = Integer.toString(day)+ "/" +
//                Integer.toString(month) + "/" +
//                Integer.toString(year);
//        int timehh = cal.get(Calendar.HOUR_OF_DAY);
//        int min = cal.get(Calendar.MINUTE);
//        int sec = cal.get(Calendar.SECOND);
//        String todo2 = ("INSERT " +
//                "           INTO    usermeasurementsfortrain " +
//                "                     (UserID, GroupID, " +
//                "                      GengroupID, GsrCur, GsrValue,  " +
//                "                      GsrAmpl, " +
//                "                      GsrLatency, " +
//                "                      DateGsr, TimeGsr," +
//                "                      MinGsr,  SecGsr, Temperature, " +
//                "                      HRate, SDNN, EmoLabelPer ) " +
//                "            VALUES ('"+Uid+"', '"+iGroupID+"', " +
//                "                    '"+iGengroupID+"','"+dbgsrcurR+"', '"+dbgsrvalue+"', " +
//                "                    '"+dbgsrAmpl+"', " +
//                "                    '"+dbgsrLate+"',  " +
//                "                    '"+strDDMMYYYY+"', '"+timehh+"', " +
//                "                    '"+min+"', '"+sec+"', '"+dbtemperatureval+"'," +
//                "                    '"+dbheartrate+"', '"+dbHRV+"', '"+pred+"')") ;
//        //        "                    '"+min+"', '"+sec+"', '"+strDact+"' )") ;
//        //println (todo2);
//        try {
//
//            l.InsRecords(todo2);
//        }
//        catch (Exception e) {
//            System.out.println("Insert Error\n"+e);
//        }
//
//
//
//    }
//
//
//
//}
