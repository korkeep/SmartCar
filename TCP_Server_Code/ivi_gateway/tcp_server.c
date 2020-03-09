#include <sys/types.h>
#include <sys/stat.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <syslog.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <sys/time.h>
#include <arpa/inet.h>
#include <pthread.h>	// 스레드 관련 함수 사용을 위해 gcc 옵션으로 -pthread
#include <net/if.h>
#include <sys/ioctl.h>
#include <netdb.h>
#include <linux/can.h>
#include <linux/can/raw.h>
#include "lib.h"

#define BUF_LEN 512		// 버퍼 최대 사이즈
#define MAX_CLIENT 1	// 최대 Client 수

unsigned const char* user_id = "mesl";
unsigned const char* user_pw = "348";
unsigned const char* chLoginSuccess = "Success";
unsigned const char* chLoginFail = "Fail";
unsigned const char* chSpeed1 = "Motor Speed : Slow\n";
unsigned const char* chSpeed2 = "Motor Speed : Normal\n";
unsigned const char* chSpeed3 = "Motor Speed : Fast\n";
unsigned const char* chStop = "Motor Speed : Stop\n";
unsigned const char* chLight0 = "Light State : Off\n";
unsigned const char* chLight1 = "Light State : On\n";
unsigned const char* chHorn = "Horn State : Ring\n";
unsigned const char* chHandle0 = "Handle State : Front\n";
unsigned const char* chHandle1 = "Handle State : Right\n";
unsigned const char* chHandle2 = "Handle State : Back\n";
unsigned const char* chHandle3 = "Handle State : Left\n";

int canSocket;								// CAN 소켓
struct can_frame canFrame;					// CAN 전송 프레임
int loginFlag = 0;							// 로그인 여부 확인
int socketList[MAX_CLIENT + 2] = { 0 };		// 소켓 인덱스 저장 배열

void* workTh(void* arg);
int parsingPacket(char* packet, int socket);
void end(int connectSocket);
void stop(int connectSocket);
void speed1(int connectSocket);
void speed2(int connectSocket);
void speed3(int connectSocket);
void light0(int connectSocket);
void light1(int connectSocket);
void horn1(int connectSocket);
void handle0(int connectSocket);
void handle1(int connectSocket);
void handle2(int connectSocket);
void handle3(int connectSocket);
void close_Socket();

int main(int argc, char* argv[])
{
	// TCP 소켓 변수
	int listenSocket, connectSocket;			// TCP 소켓 (Listen, Connect)
	struct sockaddr_in serverAddr, clientAddr;	// TCP 소켓 주소
	char connectedAddr[32];						// Client IP 주소
	int clientAddr_len;							// Client IP 주소 길이
	int sockoptbuf = 1;							// 소켓 바인딩 문제를 해결하기위한 옵션
	int sock_count = 0;							// 소켓의 개수

	// CAN 소켓 변수
	struct sockaddr_can can_addr;			// CAN 소켓 주소
	struct ifreq ifr; 						// CAN 모듈 확인

	// 스레드 변수 선언
	int th_id;
	pthread_t thread;

	// TCP 통신 Terminal 시작
	printf("IVI Gateway : IVI Gateway Started\n");

	// TCP 통신 준비
	if ((listenSocket = socket(PF_INET, SOCK_STREAM, 0)) == -1) {
		printf("TCP Socket ERR : Can't open Socket\n");
	}

	// SO_REUSEADDR 옵션을 통해 비정상적인 종료로 TIME WAIT 상태에 들어간 PORT도 재사용 가능
	setsockopt(listenSocket, SOL_SOCKET, SO_REUSEADDR, (char*)&sockoptbuf, (int)sizeof(sockoptbuf));

	// Server 소켓 초기화
	memset(&serverAddr, 0x00, sizeof(serverAddr));
	serverAddr.sin_family = AF_INET;
	serverAddr.sin_addr.s_addr = htonl(INADDR_ANY);
	serverAddr.sin_port = htons(10080 & 0xffff);

	// Server 소켓 바인딩
	if (bind(listenSocket, (struct sockaddr*) & serverAddr, sizeof(serverAddr)) < 0) {
		printf("TCP Socket ERR : Can't bind Local Address\n");
	}

	// Server 소켓 Listen
	if (listen(listenSocket, MAX_CLIENT) < 0) {
		printf("TCP Socket ERR : Can't connect ListenSocket\n");
	}

	// CAN 소켓 초기화
	if ((canSocket = socket(PF_CAN, SOCK_RAW, CAN_RAW)) < 0) {
		printf("CAN ERR : Can't open CAN Socket\n");
	}
	else {
		can_addr.can_family = AF_CAN;
		strcpy(ifr.ifr_name, "can0");
		if (ioctl(canSocket, SIOCGIFINDEX, &ifr) < 0) {
			printf("CAN ERR : Can't control CAN Kernel\n");
		}
		can_addr.can_ifindex = ifr.ifr_ifindex;

		setsockopt(canSocket, SOL_CAN_RAW, CAN_RAW_FILTER, NULL, 0);

		if (bind(canSocket, (struct sockaddr*) & can_addr, sizeof(can_addr)) < 0) {
			printf("CAN ERR : Can't bind CAN Address\n");
		}
	} // end : else

	// Server 소켓, Can 소켓을 socketList에 저장
	socketList[sock_count++] = listenSocket;	// socketList[0] → listenSocket
	socketList[sock_count++] = canSocket;		// socketList[1] → canSocket

	while (1) {
		clientAddr_len = sizeof(clientAddr);
		connectSocket = accept(listenSocket, (struct sockaddr*) & clientAddr, &clientAddr_len);

		// Client 연결 에러
		if (connectSocket < 0) {
			printf("IVI Gateway ERR : Connection Accept Failed\n");
			continue;
		}

		// Client 주소 에러
		if (inet_ntop(AF_INET, &clientAddr.sin_addr.s_addr, connectedAddr, sizeof(connectedAddr)) == NULL) {
			printf("IVI Gateway ERR : Client Address Error\n");
		}
		// Client 연결 성공
		else {
			printf("IVI Gateway : Client(%s) connected\n", connectedAddr);
			socketList[sock_count] = connectSocket;
		}

		// Client와 통신을 전담할 스레드 생성
		th_id = pthread_create(&thread, NULL, workTh, (void*)connectSocket);
		// 스레드 생성 실패
		if (th_id != 0) {
			printf("Thread ERR : Thread Create Error\n");
		}
	} // end : while(1)

	close_Socket();
	return 0;
} // end : main

// workTh : Client와의 소켓 통신을 위한 스레드
void* workTh(void* arg)
{
	int connectSocket = (int)arg;
	int	parsingResult = -1;
	unsigned char recvBuf[BUF_LEN];	// TCP Client에서 받은 데이터 저장할 버퍼

	memset(&recvBuf, 0x00, sizeof(recvBuf));

	// Client와 연결 → 메시지를 받을때까지 스레드는 대기상태
	while (read(connectSocket, recvBuf, BUF_LEN) > 0) {
		parsingResult = parsingPacket(recvBuf, connectSocket);

		// exit 명령 입력받을시 소켓 종료 + Server 재시작 
		if (parsingResult == 0) {
			printf("\nIVI Gateway : IVI Gateway Reloaded\n");
			connectSocket = -1;
			pthread_exit(NULL);
		}
	} // end : while(read(connectSocket, recvBuf, BUF_LEN) > 0)
}

// parsingPacket : Client의 패킷 분석
int parsingPacket(char* packet, int socket)
{
	int connectSocket = socket;
	char buf[24];
	char* id, * pw, * cmd = NULL;
	
	// 패킷을 버퍼에 복사
	strcpy(buf, packet);

	for (int i = 0; i < 24; i++) {
		if (buf[i] == 'A' && buf[i + 1] == 'D') {
			
			// Client가 보낸 메시지 파싱
			id = strtok(buf + (i + 2), "@");
			pw = strtok(NULL, "@");
			cmd = strtok(NULL, "DD");

			// Client가 로그인을 요청하고(login0) ID와 PW가 일치하면 로그인 성공
			if (!strcmp(cmd, "login0") && !strcmp(id, user_id) && !strcmp(pw, user_pw)) {
				printf("IVI Gateway : User %s is logged in.\n", id);
				loginFlag = 1;
				write(connectSocket, chLoginSuccess, strlen(chLoginSuccess));
				return 1;
			}

			// Clinet가 로그인 없이 IVI 제어를 시도하거나 ID나 PW가 일치하지 않을 경우 거부
			else if (strcmp(id, user_id) || strcmp(pw, user_pw) || !loginFlag) {
				printf("IVI Gateway : Please log in\n");
				write(connectSocket, chLoginFail, strlen(chLoginFail));
				return 1;
			}

			// 패킷 Log 출력
			printf("buf = %s@%s@%s\n", buf, pw, cmd);

			// 로그인이 되어있고 ID와 PW가 일치한 메시지면 명령 실행
			if (loginFlag && !strcmp(id, user_id) && !strcmp(pw, user_pw)) {
				memset(&canFrame, 0x00, sizeof(canFrame));
				// 소켓 + Server 종료 함수
				if (!strcmp(cmd, "exit")) {
					end(connectSocket);
					return 0;
				}
				// Speed 관련 함수
				else if (!strcmp(cmd, "speed0")) {
					stop(connectSocket);
				}
				else if (!strcmp(cmd, "speed1")) {
					speed1(connectSocket);
				}
				else if (!strcmp(cmd, "speed2")) {
					speed2(connectSocket);
				}
				else if (!strcmp(cmd, "speed3")) {
					speed3(connectSocket);
				}
				// Light 관련 함수
				else if (!strcmp(cmd, "light0")) {
					light0(connectSocket);
				}
				else if (!strcmp(cmd, "light1")) {
					light1(connectSocket);
				}
				// Horn 관련 함수
				else if (!strcmp(cmd, "horn1")) {
					horn1(connectSocket);
				}
				// Handle 관련 함수
				else if (!strcmp(cmd, "handle0")) {
					handle0(connectSocket);
				}
				else if (!strcmp(cmd, "handle1")) {
					handle1(connectSocket);
				}
				else if (!strcmp(cmd, "handle2")) {
					handle2(connectSocket);
				}
				else if (!strcmp(cmd, "handle3")) {
					handle3(connectSocket);
				}
				return 2;
			} // end : if(loginFlag && !strcmp(id, user_id) && !strcmp(pw, user_pw))
		} // end : if(buf[i]=='A' && buf[i+1]=='D')
	} // end : for( i = 0 ; i< 30 ; i++)
} // end : int parsingPacket(char* packet, int socket)

// end : 모니터 초기화
void end(int connectSocket)
{
	write(connectSocket, &chStop, sizeof(chStop));
	parse_canframe("123#63", &canFrame);
	write(canSocket, &canFrame, sizeof(canFrame));
	return;
}
// stop : 모터 정지
void stop(int connectSocket)
{
	write(connectSocket, &chStop, sizeof(chStop));
	parse_canframe("123#00", &canFrame);
	write(canSocket, &canFrame, sizeof(canFrame));
	return;
}
// speed1 : 모터 속도 느림
void speed1(int connectSocket)
{
	write(connectSocket, &chSpeed1, sizeof(chSpeed1));
	parse_canframe("123#01", &canFrame);
	write(canSocket, &canFrame, sizeof(canFrame));
	return;
}
// speed2 : 모터 속도 보통
void speed2(int connectSocket)
{
	write(connectSocket, &chSpeed2, sizeof(chSpeed2));
	parse_canframe("123#02", &canFrame);
	write(canSocket, &canFrame, sizeof(canFrame));
	return;
}
// speed3 : 모터 속도 빠름
void speed3(int connectSocket)
{
	write(connectSocket, &chSpeed3, sizeof(chSpeed3));
	parse_canframe("123#03", &canFrame);
	write(canSocket, &canFrame, sizeof(canFrame));
	return;
}

// light0 : 전조등 끄기
void light0(int connectSocket)
{
	write(connectSocket, &chLight0, sizeof(chLight0));
	parse_canframe("123#10", &canFrame);
	write(canSocket, &canFrame, sizeof(canFrame));
	return;
}
// light1 : 전조등 켜기
void light1(int connectSocket)
{
	write(connectSocket, &chLight1, sizeof(chLight1));
	parse_canframe("123#11", &canFrame);
	write(canSocket, &canFrame, sizeof(canFrame));
	return;
}

// horn1 : 경적 울림
void horn1(int connectSocket)
{
	write(connectSocket, &chHorn, sizeof(chHorn));
	parse_canframe("123#20", &canFrame);
	write(canSocket, &canFrame, sizeof(canFrame));
	return;
}

// handle0 : 앞방향
void handle0(int connectSocket)
{
	write(connectSocket, &chHandle0, sizeof(chHandle0));
	parse_canframe("123#30", &canFrame);
	write(canSocket, &canFrame, sizeof(canFrame));
	return;
}
// handle1 : 우측방향
void handle1(int connectSocket)
{
	write(connectSocket, &chHandle1, sizeof(chHandle1));
	parse_canframe("123#31", &canFrame);
	write(canSocket, &canFrame, sizeof(canFrame));
	return;
}
// handle2 : 뒷방향
void handle2(int connectSocket)
{
	write(connectSocket, &chHandle2, sizeof(chHandle2));
	parse_canframe("123#32", &canFrame);
	write(canSocket, &canFrame, sizeof(canFrame));
	return;
}
// handle3 : 좌측방향
void handle3(int connectSocket)
{
	write(connectSocket, &chHandle3, sizeof(chHandle3));
	parse_canframe("123#33", &canFrame);
	write(canSocket, &canFrame, sizeof(canFrame));
	return;
}

// close_Socket : 소켓 종료
void close_Socket()
{
	for (int i = 0; i < MAX_CLIENT + 2; i++) {
		if (socketList[i] != 0) {
			printf("%d Socket Closed!\n", socketList[i]);
			close(socketList[i]);
		}
	}
}