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

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class LoginActivity extends AppCompatActivity {

    private EditText et_Raspberry, et_ID, et_Password;
    private SetSocket setSocket = null;
    private Socket socket = null;

    //onDestroy 상태면 AsyncTask, Socket 종료해준다
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(socket != null){
                System.out.println("★★★★  LoginActivity : socket.close() Called !!  ★★★★");
                socket.close();
            }
            if(setSocket != null){
                System.out.println("★★★★  LoginActivity : setSocket.cancel() Called !!  ★★★★");
                setSocket.cancel(true);
            }

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
        Button btn_Login = findViewById(R.id.Login);
        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Keyboard 내려감
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);


                //EditText의 ID, PW, Raspberry IP → String 변환
                String ID = et_ID.getText().toString();
                String Password = et_Password.getText().toString();
                String Raspberry = et_Raspberry.getText().toString();

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
    }

    //TCP 통신을 위한 Socket 함수
    @SuppressLint("StaticFieldLeak")
    public class SetSocket extends AsyncTask<Void, Void, Boolean> {
        //소켓 통신을 위한 변수 + 결과값 받아오는 변수 선언
        String id = "";
        String password = "";
        String raspberry = "";
        String sendBuf = "";

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
                System.out.println("★★★★  LoginActivity : Now, we are in Async Background  ★★★★");

                //Socket 연결 (Server ← Client), port : 10080
                socket = new Socket(raspberry, 10080);

                //Login 절차 구현 → sendBuf : ID, PW, 초기 Speed 저장해서 송신
                sendBuf = "AD" + id + '@' + password + "@login0DD";
                OutputStream out = socket.getOutputStream();
                out.write(sendBuf.getBytes());
                return true;

                /*
                tcp_server.c 확인 결과, return 값을 Client 쪽에 넘겨주지 않고,
                Printf 함수를 사용해 내부 terminal 상에서 Log 통해 확인하도록 짜여있다.
                */

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
            TCPAgent.exe 프로그램에서도 ID,PW(kisa, 1234) 하드코딩 돼 있을 확률 ↑
            다른 Raspberry IP 주소가 입력된 경우의 예외처리까지 성공!!!
            */

            //ID,PW(kisa, 1234) Check
            if (!(id.equals("kisa") && password.equals("1234"))) {
                setCustomToast(LoginActivity.this, "ID, PW가 일치하지 않습니다");
            }
            else {
                //Background Exception 발생 시
                if (!result) {
                    setCustomToast(LoginActivity.this, "TCP 통신에 문제가 발생했습니다");
                }
                //정상적인 경우 MainActivity 실행
                else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            }
        }
    }

    //Custom Toast Message
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

