package com.example.graphtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private static Button login;
    private static EditText emailTxt;
    private static String email;
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailTxt = (EditText) findViewById(R.id.emailTxt);
        login = (Button) findViewById(R.id.loginBtn);
    }

    public void login(View view) {

        this.email = emailTxt.getText().toString();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
  }

    public String getEmail(){
        return email;
    }
}