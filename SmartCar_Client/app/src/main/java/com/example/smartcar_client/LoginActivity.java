package com.example.smartcar_client;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {

    private EditText et_Raspberry, et_ID, et_Password;
    private Button btn_Login;

    private Socket socket;      // TCP socket 통신
    private int port = 10080;   // 포트번호

    /*@Override
    protected void onStop() {
        super.onStop();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //EditText 부분 초기화
        et_ID = findViewById(R.id.ID);
        et_Password = findViewById(R.id.Password);
        et_Raspberry = findViewById(R.id.Raspberry);

        //Login 부분 기능 구현
        btn_Login = findViewById(R.id.Login);
        btn_Login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //EditText의 ID, PW, Raspberry IP → String 변환
                String ID = et_ID.getText().toString();
                String Password = et_Password.getText().toString();
                String Raspberry = et_Raspberry.getText().toString();

                //Raspberry IP, Port 주소로 Socket 설정
                /*try{
                    setSocket(Raspberry, port);
                } catch (IOException e){
                    e.printStackTrace();
                }*/

                //버튼 누르기만 하면 MainActivity 넘어감 (임시)
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /*
    public void setSocket(final String Raspberry, final int port) throws IOException {
        try {
            new AsyncTask<Void, Void, Void>(){
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                //TCP 연결 성공하면 다음 화면으로 넘어감
                @Override
                protected Void doInBackground(Void... params) {
                    try{
                        socket = new Socket(Raspberry, port);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                }

            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
