#include <df_can.h>
#include <SPI.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

const int LED_PIN = 4;
const int BUZZER_PIN = 5;
const int MOTOR_R_PIN = 6;
const int MOTOR_L_PIN = 7;

const int SPI_CS_PIN = 10;
MCPCAN CAN(SPI_CS_PIN);

LiquidCrystal_I2C lcd(0x27, 16, 2);

unsigned char cur_speed = '0';
char cur_handle = 'F';

//CAN 통신 및 LCD SetUP
void setup()
{
  Serial.begin(115200);
  int count = 50;
  do {
    CAN.init();
    if (CAN_OK == CAN.begin(CAN_500KBPS))    {
      Serial.println("DFROBOT's CAN BUS Shield init ok!");
      break;
    } else {
      Serial.println("DFROBOT's CAN BUS Shield init fail");
      Serial.println("Please Init CAN BUS Shield again");

      delay(100);
      if (count <= 1)
        Serial.println("Please give up trying!, trying is useless!");
    }
  } while (count--);

  pinMode(MOTOR_L_PIN, OUTPUT);
  pinMode(MOTOR_R_PIN, OUTPUT);
  pinMode(LED_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  
  lcd.begin();
  lcd.backlight();
  lcd.print("Speed:0 Front --");
  lcd.setCursor(0, 1);
  lcd.print("ID:0x---  MSG:--");
}

void loop()
{ 
  if (CAN_MSGAVAIL == CAN.checkReceive()) {
    unsigned char buf[2];
    unsigned char len = 0;
    unsigned long canId;
    CAN.readMsgBuf(&len, buf);

    canId = CAN.getCanId();
    lcd.setCursor(5, 1);
    lcd.print("---");
    lcd.setCursor(5, 1);
    lcd.print(canId, HEX);

    //Serial Monitoring
    for(int i=0; i<len; i++)
    {
      Serial.print(buf[0], HEX);
      Serial.print(' ');
      Serial.print(buf[1], HEX);
      Serial.print(' ');
    }

    switch(buf[0]){      
      //Speed 제어
      case 0: case 1: case 2: case 3: {
         if (buf[0] != cur_speed) {
          cur_speed = buf[0];
          lcd.setCursor(6, 0);
          lcd.print(cur_speed);
          lcd.setCursor(14, 1);
          lcd.print(buf[0]);
          lcd.setCursor(15, 1);
          lcd.print(' ');
          break;
        }
      }

      //LED 제어
      case 16: {
        lcd.setCursor(15, 0);
        lcd.print('-');
        lcd.setCursor(14, 1);
        lcd.print(buf[0]);
        
        digitalWrite(LED_PIN, LOW);
        return;
      }
      case 17: {
        lcd.setCursor(15, 0);
        lcd.print('O');
        lcd.setCursor(14, 1);
        lcd.print(buf[0]);
       
        digitalWrite(LED_PIN, HIGH);
        delay(750);
        return;
      }

      //Horn 제어
      case 32: {
        lcd.setCursor(14, 1);
        lcd.print(buf[0]);
        
        lcd.setCursor(14, 0);
        lcd.print('!');
        digitalWrite(BUZZER_PIN, HIGH);
        delay(1500);
        
        lcd.setCursor(14, 0);
        lcd.print('-');
        digitalWrite(BUZZER_PIN, LOW);
        return;
      }

      //Handle 제어
      case 48: {
        cur_handle = 'F';
        lcd.setCursor(8, 0);
        lcd.print("Front");
        lcd.setCursor(14, 1);
        lcd.print(buf[0]);
        break;
      }
      case 49: {
        cur_handle = 'R';
        lcd.setCursor(8, 0);
        lcd.print("Right");
        lcd.setCursor(14, 1);
        lcd.print(buf[0]);
        break;
      }
      case 50: {
        cur_handle = 'B';
        lcd.setCursor(8, 0);
        lcd.print("Back ");
        lcd.setCursor(14, 1);
        lcd.print(buf[0]);
        break;
      }
      case 51: {
        cur_handle = 'L';
        lcd.setCursor(8, 0);
        lcd.print("Left ");
        lcd.setCursor(14, 1);
        lcd.print(buf[0]);
        break;
      }

      //Exit → 초기화
      case 99: {
        cur_speed = '0';
        cur_handle = 'F';
        setup();
        return;
      }

      //이상한 값 처리 → MSG만 출력
      default : {
        lcd.setCursor(14, 1);
        lcd.print(buf[0]);
        if(buf[0]<10) {
          lcd.setCursor(15, 1);
          lcd.print(' ');
        }
        return;
      }
    }
  }
  //Speed 변화
  ctl_motor(cur_speed, cur_handle);
}

//ctl_motor : 속도, 방향 변화 + 방향은 아직 구현 못했다...
void ctl_motor(unsigned char inputed_speed, unsigned char inputed_handle)
{
  // 입력받은 방향에 따라 모터 속도 변화(F:앞 R:우측 B:뒤 L:좌측)
  switch (inputed_handle) {
    case 'F': {
      // 입력받은 값에 따라 모터 속도 변화(1:느림 2:중간 3:빠름)
      switch (inputed_speed) {
       case 1:
        digitalWrite(MOTOR_R_PIN, HIGH);
        digitalWrite(MOTOR_L_PIN, HIGH);
        delayMicroseconds(300);
        digitalWrite(MOTOR_R_PIN, LOW);
        digitalWrite(MOTOR_L_PIN, LOW);
        delayMicroseconds(300);
        break;
      case 2:
        digitalWrite(MOTOR_R_PIN, HIGH);
        digitalWrite(MOTOR_L_PIN, HIGH);
        delayMicroseconds(500);
        digitalWrite(MOTOR_R_PIN, LOW);
        digitalWrite(MOTOR_L_PIN, LOW);
        delayMicroseconds(300);
        break;
      case 3:
        digitalWrite(MOTOR_R_PIN, HIGH);
        digitalWrite(MOTOR_L_PIN, HIGH);
        delayMicroseconds(700);
        digitalWrite(MOTOR_R_PIN, LOW);
        digitalWrite(MOTOR_L_PIN, LOW);
        delayMicroseconds(300);
        break;
      }
      break;
    }
    case 'R': {
      switch (inputed_speed) {
       case 1:
        digitalWrite(MOTOR_R_PIN, HIGH);
        digitalWrite(MOTOR_L_PIN, HIGH);
        delayMicroseconds(300);
        digitalWrite(MOTOR_R_PIN, LOW);
        digitalWrite(MOTOR_L_PIN, LOW);
        delayMicroseconds(300);
        break;
      case 2:
        digitalWrite(MOTOR_R_PIN, HIGH);
        digitalWrite(MOTOR_L_PIN, HIGH);
        delayMicroseconds(500);
        digitalWrite(MOTOR_R_PIN, LOW);
        digitalWrite(MOTOR_L_PIN, LOW);
        delayMicroseconds(300);
        break;
      case 3:
        digitalWrite(MOTOR_R_PIN, HIGH);
        digitalWrite(MOTOR_L_PIN, HIGH);
        delayMicroseconds(700);
        digitalWrite(MOTOR_R_PIN, LOW);
        digitalWrite(MOTOR_L_PIN, LOW);
        delayMicroseconds(300);
        break;
      }
      break;
    }
    case 'B': {
      switch (inputed_speed) {
      case 1:
        digitalWrite(MOTOR_R_PIN, HIGH);
        digitalWrite(MOTOR_L_PIN, HIGH);
        delayMicroseconds(300);
        digitalWrite(MOTOR_R_PIN, LOW);
        digitalWrite(MOTOR_L_PIN, LOW);
        delayMicroseconds(300);
        break;
      case 2:
        digitalWrite(MOTOR_R_PIN, HIGH);
        digitalWrite(MOTOR_L_PIN, HIGH);
        delayMicroseconds(500);
        digitalWrite(MOTOR_R_PIN, LOW);
        digitalWrite(MOTOR_L_PIN, LOW);
        delayMicroseconds(300);
        break;
      case 3:
        digitalWrite(MOTOR_R_PIN, HIGH);
        digitalWrite(MOTOR_L_PIN, HIGH);
        delayMicroseconds(700);
        digitalWrite(MOTOR_R_PIN, LOW);
        digitalWrite(MOTOR_L_PIN, LOW);
        delayMicroseconds(300);
        break;
      }
      break;
    }
    case 'L':{
      switch (inputed_speed) {
       case 1:
        digitalWrite(MOTOR_R_PIN, HIGH);
        digitalWrite(MOTOR_L_PIN, HIGH);
        delayMicroseconds(300);
        digitalWrite(MOTOR_R_PIN, LOW);
        digitalWrite(MOTOR_L_PIN, LOW);
        delayMicroseconds(300);
        break;
      case 2:
        digitalWrite(MOTOR_R_PIN, HIGH);
        digitalWrite(MOTOR_L_PIN, HIGH);
        delayMicroseconds(500);
        digitalWrite(MOTOR_R_PIN, LOW);
        digitalWrite(MOTOR_L_PIN, LOW);
        delayMicroseconds(300);
        break;
      case 3:
        digitalWrite(MOTOR_R_PIN, HIGH);
        digitalWrite(MOTOR_L_PIN, HIGH);
        delayMicroseconds(700);
        digitalWrite(MOTOR_R_PIN, LOW);
        digitalWrite(MOTOR_L_PIN, LOW);
        delayMicroseconds(300);
        break;
      }
      break;
    }
  }
}
