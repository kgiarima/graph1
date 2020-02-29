package com.example.graphtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private static String uName,uSName,sex, age;
    private static Button login;
    private static EditText name,sname, ageTxt, sexTxt;
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        name = (EditText) findViewById(R.id.nameTxt);
        sname = (EditText) findViewById(R.id.snameTxt);
        ageTxt = (EditText) findViewById(R.id.ageTxt);
        sexTxt = (EditText) findViewById(R.id.sexTxt);
        login = (Button) findViewById(R.id.loginBtn);
    }

    public void login(View view) {

        this.uName = name.getText().toString();
        this.uSName = sname.getText().toString();
        this.sex = sexTxt.getText().toString();
        this.age = ageTxt.getText().toString();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
  }

    public String getUserName(){
        return uName;
    }
    public String getUserSurname(){
        return uSName;
    }

    public String getAge(){
        return age;
    }

    public String getSex(){
        return sex;
    }
}