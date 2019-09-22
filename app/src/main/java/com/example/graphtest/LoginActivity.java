package com.example.graphtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static Button login;
    private static EditText user,pass;
    private HashMap<String,String> users;
    private boolean access;
    BufferedReader br;
    String userTry, passTry;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user =  (EditText) findViewById(R.id.userTxt);
        pass =  (EditText) findViewById(R.id.passTxt);
        login = (Button) findViewById(R.id.loginBtn);

        users = new HashMap<>();
        access = false;
        br = null;
        userTry = "";
        passTry = "";

        try {
            br = new BufferedReader(new InputStreamReader( getAssets().open("users.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineUser = line.split("\\s*,\\s*");
                users.put(lineUser[0],lineUser[1]);
            }
        }catch(Exception e) {
            System.out.println("File not found");
        }finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("BufferReader could not be closed");
                }
            }
        }
    }

    public boolean match(String userIn, String passIn){

        if(users.containsKey(userIn)){
            String pw = users.get(userIn);
            if(pw.equals(passIn)){
                return true;
            }
        }
        return false;
    }

    public void login(View view) {

        userTry = user.getText().toString();
        passTry = pass.getText().toString();
        access = match(userTry, passTry);
        if (userTry.equals("") || passTry.equals("")) {
            Toast.makeText(LoginActivity.this, "Missing credentials", Toast.LENGTH_SHORT).show();
        }else{
            if (access) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
