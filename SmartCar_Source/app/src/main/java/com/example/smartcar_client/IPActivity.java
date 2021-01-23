package com.example.smartcar_client;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class IPActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);

        final EditText et_IP = findViewById(R.id.Raspberry);
        Button btn_IPSave = findViewById(R.id.IPSave);

        //IPSave 기능 구현
        btn_IPSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Keyboard 내려감
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                //et_IP에 입력된 값 temp로 받아오기
                String temp = et_IP.getText().toString();

                File file = new File(getFilesDir(), "SmartCar.txt");
                FileWriter fw = null;
                BufferedWriter buf = null;

                //SmartCar.txt → temp 값 쓰기
                try{
                    fw = new FileWriter(file); //덮어쓰기
                    buf = new BufferedWriter(fw);
                    buf.append(temp); //쓰고
                    buf.flush(); //비운다

                    buf.close();
                    fw.close();

                } catch(Exception e) {
                    e.printStackTrace();
                }

                //Activity 종료
                finish();
            }
        });
    }
}
