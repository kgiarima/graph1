package com.example.graphtest;

import android.annotation.SuppressLint;
import android.os.StrictMode;

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

public class DatabaseActivity {

    String url = "jdbc:mysql://127.0.0.1/affectandroid";
    String driver = "com.mysql.jdbc.Driver";
    String userName = "user";
    String passwd = "pass";

    public DatabaseActivity(){

    }

    public boolean connect(String aUser,String aPass) {
        String user = aUser;
        String pass = aPass;
        String query = "SELECT 'password' FROM 'users' WHERE 'username' = " + user;
        return true;
    }

}
