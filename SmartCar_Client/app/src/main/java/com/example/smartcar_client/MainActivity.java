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

    //sendBuf : private → onCreate, setSocketSpeed
    private String sendBuf = null;

    //setSocketLight : private → onCreate
    private SetSocketLight setSocketLight = null;

    //setSocketHorn : private → onCreate
    private SetSocketHorn setSocketHorn = null;

    //handle_num : private static → onCreate, setSocketHandle
    //Front : private → onCreate, setSocketHandle
    //Left : private → onCreate, setSocketHandle
    //Right : private → onCreate, setSocketHandle
    //Back : private → onCreate, setSocketHandle
    //setSocketHandle : private → onCreate
    private static int handle_num = 0;
    private RadioButton Front = null;
    private RadioButton Left = null;
    private RadioButton Right = null;
    private RadioButton Back = null;
    private SetSocketHandle setSocketHandle = null;

    //speed_num : private static → onCreate, setSocketSpeed
    //Speed_txt : private → onCreate, setSocketSpeed
    //Speed_img : private → onCreate, setSocketSpeed
    //setSocketSpeed : private → onCreate
    private static int speed_num = -1;
    private TextView Speed_txt = null;
    private ImageView Speed_img = null;
    private SetSocketSpeed setSocketSpeed = null;

    //Application 종료 → 뒤로가기 button 2번 연속해서 클릭
    private long time= 0;
    @Override
    public void onBackPressed(){
        //속도가 0일 때 + Power-Off 상태일 때
        if(speed_num < 1){
            if(System.currentTimeMillis()-time>=1000){
                time=System.currentTimeMillis();
                setCustomToast(MainActivity.this, "버튼을 한 번 더 누르면 완전히 종료합니다.");
            }
            else if(System.currentTimeMillis()-time<1000){
                System.out.println("★★★★  MainActivity : Process Terminated  ★★★★");

                //Shutdown : sendBuf → ID, PW, Exit 저장해서 송신
                sendBuf = sendBuf.substring(0, sendBuf.lastIndexOf("@"));
                sendBuf = sendBuf + "@exit" + "DD";

                //Process 종료
                moveTaskToBack(true);
                finishAndRemoveTask();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
        else{
            setCustomToast(MainActivity.this, "속도를 멈춘 후 시도하세요.");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        sendBuf = intent.getStringExtra("sendBuf");

        //초기화
        Speed_txt = (TextView) findViewById(R.id.Speed);
        Speed_img = (ImageView) findViewById(R.id.Speed_img);
        Front = (RadioButton) findViewById(R.id.Front);
        Left = (RadioButton) findViewById(R.id.Left);
        Right = (RadioButton) findViewById(R.id.Right);
        Back = (RadioButton) findViewById(R.id.Back);
        final ToggleButton Power = (ToggleButton) this.findViewById(R.id.Power);
        final ToggleButton Light = (ToggleButton) this.findViewById(R.id.Light);
        final ImageButton Horn = (ImageButton) this.findViewById(R.id.Horn);
        final ImageButton Plus = (ImageButton) this.findViewById(R.id.Plus);
        final ImageButton Minus = (ImageButton) this.findViewById(R.id.Minus);


        //Power 기능 구현
        Power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Power.isChecked()){
                    Power.setBackgroundResource(R.drawable.power_on);
                    speed_num = 0;
                    //TCP 통신을 위한 Socket 함수 실행
                    try {
                        setSocketSpeed = new SetSocketSpeed(speed_num);
                        setSocketSpeed.execute();
                    } catch (Exception e) {
                        System.out.println("★★★★  MainActivity : Power-On setSocketSpeed Exception Occurred !!  ★★★★");
                        e.printStackTrace();
                    }
                    setCustomToast(MainActivity.this, "Power ON");
                }
                else {
                    //속도가 0일 때, Power-Off 가능
                    if(speed_num==0){
                        Power.setBackgroundResource(R.drawable.power_off);
                        speed_num = -1;
                        //TCP 통신을 위한 Socket 함수 실행
                        try {
                            setSocketSpeed = new SetSocketSpeed(speed_num);
                            setSocketSpeed.execute();
                            setCustomToast(MainActivity.this, "Power OFF");
                        } catch (Exception e) {
                            System.out.println("★★★★  MainActivity : Power-Off setSocketSpeed Exception Occurred !!  ★★★★");
                            e.printStackTrace();
                        }
                    }
                    else {
                        Power.setChecked(true);
                        setCustomToast(MainActivity.this, "속도를 멈춘 후 시도하세요.");
                    }
                }
            }
        });

        //Light 기능 구현
        Light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Light.isChecked()){
                    Light.setBackgroundResource(R.drawable.light_on);
                    try {
                        setSocketLight = new SetSocketLight(1);
                        setSocketLight.execute();
                    } catch (Exception e) {
                        System.out.println("★★★★  MainActivity : Light-On setSocketLight Exception Occurred !!  ★★★★");
                        e.printStackTrace();
                    }
                    setCustomToast(MainActivity.this, "Light ON");
                } else {
                    Light.setBackgroundResource(R.drawable.light_off);
                    try {
                        setSocketLight = new SetSocketLight(0);
                        setSocketLight.execute();
                    } catch (Exception e) {
                        System.out.println("★★★★  MainActivity : Light-Off setSocketLight Exception Occurred !!  ★★★★");
                        e.printStackTrace();
                    }
                    setCustomToast(MainActivity.this, "Light OFF");
                }
            }
        });

        //Horn 기능 구현
        Horn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Horn.setBackgroundResource(R.drawable.horn_on);
                try {
                    setSocketHorn = new SetSocketHorn(1);
                    setSocketHorn.execute();
                } catch (Exception e) {
                    System.out.println("★★★★  MainActivity : setSocketHorn Exception Occurred !!  ★★★★");
                    e.printStackTrace();
                }
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
                if(handle_num != 0 && Front.isChecked()){
                    try {
                        handle_num = 0;
                        setSocketHandle = new SetSocketHandle(handle_num);
                        setSocketHandle.execute();
                    } catch (Exception e) {
                        System.out.println("★★★★  MainActivity : Handle-Front setSocketHandle Exception Occurred !!  ★★★★");
                        e.printStackTrace();
                    }
                }
            }
        });
        Right.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(handle_num != 1 && Right.isChecked()){
                    try {
                        handle_num = 1;
                        setSocketHandle = new SetSocketHandle(handle_num);
                        setSocketHandle.execute();
                    } catch (Exception e) {
                        System.out.println("★★★★  MainActivity : Handle-Right setSocketHandle Exception Occurred !!  ★★★★");
                        e.printStackTrace();
                    }
                }
            }
        });
        Back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(handle_num != 2 && Back.isChecked()){
                    try {
                        handle_num = 2;
                        setSocketHandle = new SetSocketHandle(handle_num);
                        setSocketHandle.execute();
                    } catch (Exception e) {
                        System.out.println("★★★★  MainActivity : Handle-Back setSocketHandle Exception Occurred !!  ★★★★");
                        e.printStackTrace();
                    }
                }
            }
        });
        Left.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(handle_num != 3 && Left.isChecked()){
                    try {
                        handle_num = 3;
                        setSocketHandle = new SetSocketHandle(handle_num);
                        setSocketHandle.execute();
                    } catch (Exception e) {
                        System.out.println("★★★★  MainActivity : Handle-Left setSocketHandle Exception Occurred !!  ★★★★");
                        e.printStackTrace();
                    }
                }
            }
        });

        //Speed++ 기능 구현
        Plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Power.isChecked()){
                    if(speed_num < 3) {
                        Plus.setBackgroundResource(R.drawable.plus_on);
                        setCustomToast(MainActivity.this, "Speed UP");
                        speed_num++;

                        //TCP 통신을 위한 Socket 함수 실행
                        try {
                            setSocketSpeed = new SetSocketSpeed(speed_num);
                            setSocketSpeed.execute();
                        } catch (Exception e) {
                            System.out.println("★★★★  MainActivity : Speed-Up setSocketSpeed Exception Occurred !!  ★★★★");
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
                    else {
                        setCustomToast(MainActivity.this, "MAX Speed");
                    }
                }
                else {
                    setCustomToast(MainActivity.this, "Power State is OFF");
                }
            }
        });

        //Speed-- 기능 구현
        Minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Power.isChecked()){
                    if(speed_num > 0) {
                        Minus.setBackgroundResource(R.drawable.minus_on);
                        setCustomToast(MainActivity.this, "Speed DOWN");
                        speed_num--;

                        //TCP 통신을 위한 Socket 함수 실행
                        try {
                            setSocketSpeed = new SetSocketSpeed(speed_num);
                            setSocketSpeed.execute();
                        } catch (Exception e) {
                            System.out.println("★★★★  MainActivity : Speed-Down setSocketSpeed Exception Occurred !!  ★★★★");
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
                    else {
                        setCustomToast(MainActivity.this, "MIN Speed");
                    }
                }
                else {
                    setCustomToast(MainActivity.this, "Power State is OFF");
                }
            }
        });
    }

    //TCP 통신을 위한 SetSocketSpeed 함수
    @SuppressLint("StaticFieldLeak")
    private class SetSocketSpeed extends AsyncTask<Void, Void, Boolean> {
        //Speed
        Integer speed = -1;

        //Constructor
        SetSocketSpeed(Integer Speed) {
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
                System.out.println("★★★★  MainActivity : Now, we are in SetSocketSpeed  ★★★★");

                //Power-Off : sendBuf 없이, return true
                if (speed==-1) { return true; }

                //Power-On : sendBuf → ID, PW, Speed 저장해서 송신
                sendBuf = sendBuf.substring(0, sendBuf.lastIndexOf("@"));
                sendBuf = sendBuf + "@speed" + speed + "DD";

                OutputStream out = socket.getOutputStream();
                out.write(sendBuf.getBytes());

                return true;

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("★★★★  MainActivity : SetSocketSpeed UnknownHostException Occurred !!  ★★★★");
                return false;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("★★★★  MainActivity : SetSocketSpeed IOException Occurred !!  ★★★★");
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
            else {
                //Speed_txt, Speed_img 설정
                switch(speed) {
                    case 0:
                        Speed_txt.setText(String.valueOf(speed));
                        Speed_img.setBackgroundResource(R.drawable.speed_0);
                        break;
                    case 1:
                        Speed_txt.setText(String.valueOf(speed));
                        Speed_img.setBackgroundResource(R.drawable.speed_1);
                        break;
                    case 2:
                        Speed_txt.setText(String.valueOf(speed));
                        Speed_img.setBackgroundResource(R.drawable.speed_2);
                        break;
                    case 3:
                        Speed_txt.setText(String.valueOf(speed));
                        Speed_img.setBackgroundResource(R.drawable.speed_3);
                        break;
                    case -1:
                        Speed_txt.setText("OFF");
                        Speed_img.setBackgroundResource(R.drawable.speed);
                        return;
                }
            }
            speed = -1;
        }
    }

    //TCP 통신을 위한 SetSocketLight 함수
    @SuppressLint("StaticFieldLeak")
    private class SetSocketLight extends AsyncTask<Void, Void, Boolean> {
        //Light
        Integer light = -1;

        //Constructor
        SetSocketLight(Integer Light) {
            light = Light;
        }

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        //Background TCP 연결 시도
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                System.out.println("★★★★  MainActivity : Now, we are in SetSocketLight  ★★★★");

                //sendBuf → ID, PW, Light 저장해서 송신
                sendBuf = sendBuf.substring(0, sendBuf.lastIndexOf("@"));
                sendBuf = sendBuf + "@light" + light + "DD";

                OutputStream out = socket.getOutputStream();
                out.write(sendBuf.getBytes());
                light = -1;

                return true;

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("★★★★  MainActivity : SetSocketLight UnknownHostException Occurred !!  ★★★★");
                return false;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("★★★★  MainActivity : SetSocketLight IOException Occurred !!  ★★★★");
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
        }
    }

    //TCP 통신을 위한 SetSocketHorn 함수
    @SuppressLint("StaticFieldLeak")
    private class SetSocketHorn extends AsyncTask<Void, Void, Boolean> {
        //Horn
        Integer horn = -1;

        //Constructor
        SetSocketHorn(Integer Horn) { horn = Horn; }

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        //Background TCP 연결 시도
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                System.out.println("★★★★  MainActivity : Now, we are in SetSocketHorn  ★★★★");

                //sendBuf → ID, PW, Horn 저장해서 송신
                sendBuf = sendBuf.substring(0, sendBuf.lastIndexOf("@"));
                sendBuf = sendBuf + "@horn" + horn + "DD";

                OutputStream out = socket.getOutputStream();
                out.write(sendBuf.getBytes());
                horn = -1;

                return true;

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("★★★★  MainActivity : SetSocketHorn UnknownHostException Occurred !!  ★★★★");
                return false;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("★★★★  MainActivity : SetSocketHorn IOException Occurred !!  ★★★★");
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
        }
    }

    //TCP 통신을 위한 SetSocketHandle 함수
    @SuppressLint("StaticFieldLeak")
    private class SetSocketHandle extends AsyncTask<Void, Void, Boolean> {
        //Handle
        Integer handle = -1;

        //Constructor
        SetSocketHandle(Integer Handle) { handle = Handle; }

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        //Background TCP 연결 시도
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                System.out.println("★★★★  MainActivity : Now, we are in SetSocketHandle  ★★★★");

                //sendBuf → ID, PW, Handle 저장해서 송신
                sendBuf = sendBuf.substring(0, sendBuf.lastIndexOf("@"));
                sendBuf = sendBuf + "@handle" + handle + "DD";

                OutputStream out = socket.getOutputStream();
                out.write(sendBuf.getBytes());

                return true;

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("★★★★  MainActivity : SetSocketHandle UnknownHostException Occurred !!  ★★★★");
                return false;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("★★★★  MainActivity : SetSocketHandle IOException Occurred !!  ★★★★");
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
            }else {
                //Front, Right, Back, Left 설정
                switch(handle) {
                    case 0:
                        Front.setChecked(true);
                        Right.setChecked(false);
                        Back.setChecked(false);
                        Left.setChecked(false);
                        break;
                    case 1:
                        Front.setChecked(false);
                        Right.setChecked(true);
                        Back.setChecked(false);
                        Left.setChecked(false);
                        break;
                    case 2:
                        Front.setChecked(false);
                        Right.setChecked(false);
                        Back.setChecked(true);
                        Left.setChecked(false);
                        break;
                    case 3:
                        Front.setChecked(false);
                        Right.setChecked(false);
                        Back.setChecked(false);
                        Left.setChecked(true);
                        break;
                }
            }
            handle = -1;
        }
    }
}
