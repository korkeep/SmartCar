package com.example.smartcar_client;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;

public class MainActivity extends LoginActivity {

    //sendBuf : private → onCreate, setSocket
    //setSocket : private → onCreate, onDestroy
    private String sendBuf = null;
    private SetSocket setSocket = null;

    //num : private static → onCreate, setSocket
    //Speed_txt : private → onCreate, setSocket
    //Speed_img : private → onCreate, setSocket
    private static int num = -1;
    private TextView Speed_txt = null;
    private ImageView Speed_img = null;

    //AsyncTask 종료
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(setSocket != null){
            System.out.println("★★★★  MainActivity : setSocket.cancel() Called !!  ★★★★");
            setSocket.cancel(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        sendBuf = intent.getStringExtra("sendBuf");

        //초기화
        Speed_txt = (TextView)findViewById(R.id.Speed);
        Speed_img = (ImageView) findViewById(R.id.Speed_img);
        final ToggleButton Power = (ToggleButton) this.findViewById(R.id.Power);
        final ToggleButton Light = (ToggleButton) this.findViewById(R.id.Light);
        final ImageButton Horn = (ImageButton) this.findViewById(R.id.Horn);
        final RadioButton Front = (RadioButton) this.findViewById(R.id.Front);
        final RadioButton Left = (RadioButton) this.findViewById(R.id.Left);
        final RadioButton Right = (RadioButton) this.findViewById(R.id.Right);
        final RadioButton Back = (RadioButton) this.findViewById(R.id.Back);
        final ImageButton Plus = (ImageButton) this.findViewById(R.id.Plus);
        final ImageButton Minus = (ImageButton) this.findViewById(R.id.Minus);

        //Power 기능 구현
        Power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Power.isChecked()){
                    Power.setBackgroundResource(R.drawable.power_on);
                    num = 0;
                    //TCP 통신을 위한 Socket 함수 실행
                    try {
                        setSocket = new SetSocket(num);
                        setSocket.execute();
                    } catch (Exception e) {
                        System.out.println("★★★★  MainActivity : Power-On setSocket Exception Occurred !!  ★★★★");
                        e.printStackTrace();
                    }

                    setCustomToast(MainActivity.this, "Power ON");
                }else{
                    Power.setBackgroundResource(R.drawable.power_off);
                    num = -1;
                    //TCP 통신을 위한 Socket 함수 실행
                    try {
                        setSocket = new SetSocket(num);
                        setSocket.execute();
                    } catch (Exception e) {
                        System.out.println("★★★★  MainActivity : Power-Off setSocket Exception Occurred !!  ★★★★");
                        e.printStackTrace();
                    }

                    setCustomToast(MainActivity.this, "Power OFF");
                }
            }
        });

        //Light 기능 구현
        Light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Light.isChecked()){
                    Light.setBackgroundResource(R.drawable.light_on);
                    setCustomToast(MainActivity.this, "Light ON");
                }else{
                    Light.setBackgroundResource(R.drawable.light_off);
                    setCustomToast(MainActivity.this, "Light OFF");
                }
            }
        });

        //Horn 기능 구현
        Horn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Horn.setBackgroundResource(R.drawable.horn_on);
                setCustomToast(MainActivity.this, "빵빵!!");

                // Handler 1초간 Delay 주기
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Horn.setBackgroundResource(R.drawable.horn_off);
                    }
                }, 1200);
            }
        });

        //Handle 기능 구현
        Front.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(Front.isChecked()){
                    Left.setChecked(false);
                    Right.setChecked(false);
                    Back.setChecked(false);
                }
            }
        });
        Left.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(Left.isChecked()){
                    Front.setChecked(false);
                    Right.setChecked(false);
                    Back.setChecked(false);
                }
            }
        });
        Right.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(Right.isChecked()){
                    Front.setChecked(false);
                    Left.setChecked(false);
                    Back.setChecked(false);
                }
            }
        });
        Back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(Back.isChecked()){
                    Front.setChecked(false);
                    Left.setChecked(false);
                    Right.setChecked(false);
                }
            }
        });

        //Speed++ 기능 구현
        Plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Power.isChecked()){
                    if(num < 3)
                    {
                        Plus.setBackgroundResource(R.drawable.plus_on);
                        setCustomToast(MainActivity.this, "Speed UP");
                        num++;

                        //TCP 통신을 위한 Socket 함수 실행
                        try {
                            setSocket = new SetSocket(num);
                            setSocket.execute();
                        } catch (Exception e) {
                            System.out.println("★★★★  MainActivity : Speed-Up setSocket Exception Occurred !!  ★★★★");
                            e.printStackTrace();
                        }

                        // Handler 0.1초간 Delay 주기
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                Plus.setBackgroundResource(R.drawable.plus_off);
                            }
                        }, 100);
                    }
                    else
                    {
                        setCustomToast(MainActivity.this, "MAX Speed");
                    }
                }
                else
                {
                    setCustomToast(MainActivity.this, "Power State is OFF");
                }
            }
        });

        //Speed-- 기능 구현
        Minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Power.isChecked()){
                    if(num > 0)
                    {
                        Minus.setBackgroundResource(R.drawable.minus_on);
                        setCustomToast(MainActivity.this, "Speed DOWN");
                        num--;

                        //TCP 통신을 위한 Socket 함수 실행
                        try {
                            setSocket = new SetSocket(num);
                            setSocket.execute();
                        } catch (Exception e) {
                            System.out.println("★★★★  MainActivity : Speed-Down setSocket Exception Occurred !!  ★★★★");
                            e.printStackTrace();
                        }

                        // Handler 0.1초간 Delay 주기
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                Minus.setBackgroundResource(R.drawable.minus_off);
                            }
                        }, 100);
                    }
                    else
                    {
                        setCustomToast(MainActivity.this, "MIN Speed");
                    }
                }
                else
                {
                    setCustomToast(MainActivity.this, "Power State is OFF");
                }
            }
        });
    }

    //TCP 통신을 위한 Socket 함수
    @SuppressLint("StaticFieldLeak")
    private class SetSocket extends AsyncTask<Void, Void, Boolean> {
        //Speed
        Integer speed = -1;
        //Sensor 추가해서, Light, Horn, Handle 등 부가 기능 추가

        //Constructor
        SetSocket(Integer Speed) {
            speed = Speed;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //Background TCP 연결 시도
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                System.out.println("★★★★  MainActivity : Now, we are in Async Background  ★★★★");

                //시동 꺼짐 : sendBuf 없이 return true
                if(num==-1){
                    return true;
                }

                //시동 켜짐 : sendBuf → ID, PW, Speed 저장해서 송신
                sendBuf = sendBuf.substring(0, sendBuf.lastIndexOf("@"));
                sendBuf = sendBuf + "@speed" + num + "DD";

                OutputStream out = socket.getOutputStream();
                out.write(sendBuf.getBytes());
                return true;

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("★★★★  MainActivity : Background UnknownHostException Occurred !!  ★★★★");
                return false;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("★★★★  MainActivity : Background IOException Occurred !!  ★★★★");
                return false;
            }
        }

        //Background 실행 후 결과
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            //Background Exception
            if (!result) {
                setCustomToast(MainActivity.this, "TCP 통신에 문제가 발생했습니다");
            }
            else{
                //Speed_txt, Speed_img 설정
                switch(num){
                    case -1:
                        Speed_txt.setText("OFF");
                        Speed_img.setBackgroundResource(R.drawable.speed);
                        break;
                    case 0:
                        Speed_txt.setText(String.valueOf(num));
                        Speed_img.setBackgroundResource(R.drawable.speed_0);
                        break;
                    case 1:
                        Speed_txt.setText(String.valueOf(num));
                        Speed_img.setBackgroundResource(R.drawable.speed_1);
                        break;
                    case 2:
                        Speed_txt.setText(String.valueOf(num));
                        Speed_img.setBackgroundResource(R.drawable.speed_2);
                        break;
                    case 3:
                        Speed_txt.setText(String.valueOf(num));
                        Speed_img.setBackgroundResource(R.drawable.speed_3);
                }
            }
        }
    }
}
