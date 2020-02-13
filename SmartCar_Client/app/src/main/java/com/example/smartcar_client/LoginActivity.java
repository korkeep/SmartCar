package com.example.smartcar_client;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private EditText et_Raspberry, et_ID, et_Password;
    private Button btn_Login;

    private Socket socket;      // TCP socket 통신
    private int port = 10080;   // 포트번호

    //onStop 상태면 Socket 종료해준다
    @Override
    protected void onStop() {
        super.onStop();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //EditText 초기화
        et_ID = findViewById(R.id.ID);
        et_Password = findViewById(R.id.Password);
        et_Raspberry = findViewById(R.id.Raspberry);

        //Login 기능 구현
        btn_Login = findViewById(R.id.Login);
        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //EditText의 ID, PW, Raspberry IP → String 변환
                String ID = et_ID.getText().toString();
                String Password = et_Password.getText().toString();
                String Raspberry = et_Raspberry.getText().toString();

                //TCP 통신을 위한 Socket 함수 실행
                try {
                    SetSocket setSocket = new SetSocket(Raspberry, ID, Password);
                    setSocket.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //TCP 통신을 위한 Socket 함수
    @SuppressLint("StaticFieldLeak")
    public class SetSocket extends AsyncTask<Void, Void, Void> {
        //소켓 통신을 위한 변수 + 결과값 받아오는 변수 선언
        String id = "";
        String password = "";
        String raspberry = "";
        String sendBuf = "";
        StringBuffer response = new StringBuffer();

        //Constructor
        SetSocket(String Rasp, String ID, String PW) {
            raspberry = Rasp;
            password = PW;
            id = ID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //Background TCP 연결 시도
        @Override
        protected Void doInBackground(Void... params) {
            try {
                System.out.println("★★★★  Now, we are in Async Background  ★★★★");

                //Socket 연결 (Server ← Client)
                socket = new Socket(raspberry, port);

                //Login 절차 구현 → sendBuf : ID, PW, 초기 Speed 저장해서 송신
                sendBuf = "AD" + id + '@' + password + "@login0DD";
                OutputStream out = socket.getOutputStream();
                out.write(sendBuf.getBytes());

                //Raspberry(Server) Data 수신
                /*ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];
                int bytesRead;
                InputStream inputStream = socket.getInputStream();
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response.append(byteArrayOutputStream.toString("UTF-8"));
                }
                System.out.println("    Data 수신 성공");*/

                //응답이 ~~라면 로그인 성공, 화면 전환
                //임시 화면 전환
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response.append("UnknownHostException: " + e.toString());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response.append("IOException: " + e.toString());
            }
            System.out.println("Server Response :   " + response);
            return null;
        }

        //Background 실행 후 결과
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //MainActivity 넘어감
                /*Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);*/
        }
    }
}

