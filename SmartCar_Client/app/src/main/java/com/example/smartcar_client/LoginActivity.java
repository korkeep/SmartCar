package com.example.smartcar_client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class LoginActivity extends AppCompatActivity {

    //socket : public static → MainActivity, onCreate, onDestroy
    //setSocket : private → onCreate, onDestroy
    //sendBuf : private → onCreate, setSocketSpeed
    public static Socket socket = null;
    private SetSocket setSocket = null;
    private String sendBuf = null;

    //AsyncTask, Socket 종료
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("★★★★  LoginActivity : Process Terminated  ★★★★");

        //Shutdown : sendBuf → ID, PW, Exit 저장해서 송신
        SetSocketDestroy setSocketDestroy = new SetSocketDestroy();
        setSocketDestroy.execute();

        //Process 종료
        moveTaskToBack(true);
        finishAndRemoveTask();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //초기화
        final EditText et_ID = findViewById(R.id.ID);
        final EditText et_Password = findViewById(R.id.Password);
        Button btn_Login = findViewById(R.id.Login);
        Button btn_IPSet = findViewById(R.id.IPSet);

        //Login 기능 구현
        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Keyboard 내려감
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                //EditText의 ID, PW → String 변환
                String ID = et_ID.getText().toString();
                String Password = et_Password.getText().toString();

                //SmartCar.txt의 Raspberry IP → 읽어오기
                String Raspberry = null;
                File file = new File(getFilesDir(), "SmartCar.txt");
                //SmartCar.txt 파일 읽어오기
                if(file.exists()){
                    try{
                        String line = null;
                        FileReader fr = new FileReader(file);
                        BufferedReader buf = new BufferedReader(fr);

                        Raspberry = buf.readLine();

                        buf.close();
                        fr.close();

                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                //TCP 통신을 위한 Socket 함수 실행
                try {
                    setSocket = new SetSocket(Raspberry, ID, Password);
                    setSocket.execute();

                } catch (Exception e) {
                    System.out.println("★★★★  LoginActivity : setSocket Exception Occurred !!  ★★★★");
                    e.printStackTrace();
                }
            }
        });

        //IPSet 기능 구현
        btn_IPSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Keyboard 내려감
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                //IPActivity 시작
                Intent intent = new Intent(getApplicationContext(), IPActivity.class);
                startActivity(intent);
            }
        });
    }

    //TCP 통신을 위한 Socket 함수
    @SuppressLint("StaticFieldLeak")
    private class SetSocket extends AsyncTask<Void, Void, Boolean> {
        //IP, ID, PW, sendBuf
        String id = "";
        String password = "";
        String raspberry = "";

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
        protected Boolean doInBackground(Void... params) {
            try {
                System.out.println("★★★★  LoginActivity : Now, we are in SetSocket  ★★★★");

                /*
                tcp_server.c 확인 결과, return 값을 Client 쪽에 넘겨주지 않고,
                Printf 함수를 사용해 내부 terminal 상에서 Log 통해 확인하도록 짜여있다.
                */

                //Socket 연결 (Server ← Client), port : 10080
                socket = new Socket(raspberry, 10080);

                //Login 절차 구현 → sendBuf : ID, PW, 초기 Speed 저장해서 송신
                sendBuf = "AD" + id + '@' + password + "@login0DD";
                OutputStream out = socket.getOutputStream();
                out.write(sendBuf.getBytes());
                return true;

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("★★★★  LoginActivity : Background UnknownHostException Occurred !!  ★★★★");
                return false;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("★★★★  LoginActivity : Background IOException Occurred !!  ★★★★");
                return false;
            }
        }

        //Background 실행 후 결과
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            /*
            tcp_server.c의 ID,PW(mesl, 348)를 변경해서 컴파일 후 동작시켜 보니,
            TCPAgent.exe 프로그램 자체에서 Login 시도를 할 수 없도록 막고 있었다.
            TCPAgent.exe 프로그램에서도 ID,PW(mesl, 348) 하드코딩 돼 있을 확률 ↑
            다른 Raspberry IP 주소가 입력된 경우의 예외처리까지 성공!!!
            */

            //ID,PW(mesl, 348) Check
            if (!(id.equals("mesl") && password.equals("348"))) {
                setCustomToast(LoginActivity.this, "ID, PW가 일치하지 않습니다");
            }
            else {
                //Background Exception
                if (!result) {
                    setCustomToast(LoginActivity.this, "IP 주소를 올바르게 설정해주세요");
                }
                //intent → MainActivity 실행, sendBuf
                else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("sendBuf", sendBuf);
                    startActivity(intent);
                }
            }
        }
    }

    //TCP 통신을 끊기 위한 SetSocketDestroy 함수
    @SuppressLint("StaticFieldLeak")
    private class SetSocketDestroy extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        //Background TCP 연결 끊기 시도
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                System.out.println("★★★★  LoginActivity : Now, we are in SetSocketDestroy  ★★★★");

                //Shutdown : sendBuf → ID, PW, Exit 저장해서 송신
                sendBuf = sendBuf.substring(0, sendBuf.lastIndexOf("@"));
                sendBuf = sendBuf + "@exit" + "DD";

                OutputStream out = socket.getOutputStream();
                out.write(sendBuf.getBytes());

                return true;

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("★★★★  LoginActivity : SetSocketDestroy UnknownHostException Occurred !!  ★★★★");
                return false;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("★★★★  LoginActivity : SetSocketDestroy IOException Occurred !!  ★★★★");
                return false;
            }
        }

        //Background 실행 후 결과
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            //Background Exception
            if (!result) {
                setCustomToast(LoginActivity.this, "TCP 통신에 문제가 발생했습니다");
            }
        }
    }

    //Custom Toast Message : public static → LoginActivity, MainActivity
    public static void setCustomToast(Context context, String msg) {
        TextView m_temp = new TextView(context);
        m_temp.setBackgroundResource(R.color.colorItem);
        m_temp.setPadding(32, 32, 32, 32);
        m_temp.setTextSize(16);
        m_temp.setText(msg);

        final Toast toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 48);
        toast.setView(m_temp);
        toast.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 1000);
    }
}

