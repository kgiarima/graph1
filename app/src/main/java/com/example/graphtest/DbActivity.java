package com.example.graphtest;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbActivity {

    String url = "jdbc:mysql://localhost/affectandroid";
//    String url = "";
    String dbName = "";
    String driver = "com.mysql.jdbc.Driver";
    String userName = "user";
    String passwd = "pass";
    String pempty = "-";
    String fname="";

    private Connection conn = null;




    public DbActivity()
    {


        try
        {
            String currentDir = new File("").getAbsolutePath();
            System.out.println(currentDir);
            fname = currentDir + "\\gsr.ini";

            FileInputStream fstream = new FileInputStream(fname);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line

            if ((strLine = br.readLine()) != null)
            {
                userName = strLine;

            }
            if ((strLine = br.readLine()) != null)
            {
                passwd = strLine;

                if (passwd.equals(pempty)) passwd = "";

            }
            if ((strLine = br.readLine()) != null)
            {
                String str="?useUnicode=yes&characterEncoding=UTF-8";
                url = strLine + str;

            }
            //Close the input stream
            in.close();


        }
        catch (Exception e)
        {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }




    }





    public void dbopen()
    {

        try
        {



            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url+dbName,userName,passwd);


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }





    public void dbclose()
    {
        if (conn != null)
        {
            try
            {

                conn.close();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }

    public ResultSet SelRecords(String query)
    {
        Statement stmt = null;
        ResultSet rset = null;

        try
        {

            dbopen();
            stmt=conn.createStatement();

            rset = stmt.executeQuery(query);


        }

        catch (SQLException e)
        {
            System.err.println("Cannot connect ! ");
            e.printStackTrace();
        }
		 /*
		 finally {
		     System.out.println("Closing the connection.");
		     if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
		 }
		 */
        return rset;

    }


    public void InsRecords(String query)
    {

        Statement stmt = null;

        try
        {

            dbopen();
            stmt=conn.createStatement();


            stmt.executeUpdate(query);
            System.out.println("Record Inserted Successfully");
        }

        catch (SQLException e)
        {
            System.err.println("Cannot connect ! ");
            e.printStackTrace();
        }

        finally {
            System.out.println("Closing the connection.");
            if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
        }

    }

    public void UpdRecords(String query)
    {

        Statement stmt = null;

        try
        {

            dbopen();
            stmt=conn.createStatement();
            //Step 3 : SQL Query
            //String updatequery="UPDATE ITEM SET PRODUCT='UsbCable' Where ID=1";



            //Step 4 : Run Query

            stmt.executeUpdate(query);
            System.out.println("Table Updated Successfully");
        }

        catch (SQLException e)
        {
            System.err.println("Cannot connect ! ");
            e.printStackTrace();
        }

        finally {
            System.out.println("Closing the connection.");
            if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
        }

    }

    public void DelRecords(String query)
    {

        Statement stmt = null;

        try
        {


            dbopen();
            stmt=conn.createStatement();


            stmt.executeUpdate(query);
            System.out.println("Records Deleted Successfully");
        }

        catch (SQLException e)
        {
            System.err.println("Cannot connect ! ");
            e.printStackTrace();
        }

        finally {
            System.out.println("Closing the connection.");
            if (conn != null) try { conn.close(); } catch (SQLException ignore) {}
        }

    }
}
