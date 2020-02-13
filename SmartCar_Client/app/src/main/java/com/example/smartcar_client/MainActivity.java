package com.example.smartcar_client;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    //속도
    private static int num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //버튼 초기화
        final ToggleButton Power = (ToggleButton) this.findViewById(R.id.Power);
        final ToggleButton Light = (ToggleButton) this.findViewById(R.id.Light);
        final ImageButton Horn = (ImageButton) this.findViewById(R.id.Horn);
        final RadioButton Front = (RadioButton) this.findViewById(R.id.Front);
        final RadioButton Left = (RadioButton) this.findViewById(R.id.Left);
        final RadioButton Right = (RadioButton) this.findViewById(R.id.Right);
        final RadioButton Back = (RadioButton) this.findViewById(R.id.Back);
        final ImageButton Plus = (ImageButton) this.findViewById(R.id.Plus);
        final ImageButton Minus = (ImageButton) this.findViewById(R.id.Minus);

        //속도
        final TextView Speed = (TextView)findViewById(R.id.Speed);

        Power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Power.isChecked()){
                    Speed.setText(String.valueOf(num));
                    Power.setBackgroundResource(R.drawable.power_on);
                    setCustomToast(MainActivity.this, "Power ON");
                }else{
                    num = 0;
                    Speed.setText("OFF");
                    Power.setBackgroundResource(R.drawable.power_off);;
                    setCustomToast(MainActivity.this, "Power OFF");
                }
            }
        });
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

        Plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Power.isChecked()){
                    if(num < 3)
                    {
                        Plus.setBackgroundResource(R.drawable.plus_on);
                        setCustomToast(MainActivity.this, "Speed UP");
                        Speed.setText(String.valueOf(++num));

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
        Minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Power.isChecked()){
                    if(num > 0)
                    {
                        Minus.setBackgroundResource(R.drawable.minus_on);
                        setCustomToast(MainActivity.this, "Speed DOWN");
                        Speed.setText(String.valueOf(--num));

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

    //Custom Toast Message
    public static void setCustomToast(Context context, String msg) {
        TextView m_temp = new TextView(context);
        m_temp.setBackgroundResource(R.color.colorItem);
        m_temp.setPadding(32,32,32,32);
        m_temp.setTextSize(16);
        m_temp.setText(msg);

        final Toast toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM, 0, 48);
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
