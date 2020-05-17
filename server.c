#include <unistd.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>
#include <netdb.h>
#include <pthread.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <sqlite3.h> /*libsqlite3-dev*/
#include "lines.h"
#include "myrpc.h"

#define h_addr h_addr_list[0]
/*GLOBAL VARIABLES*/
//int rc;	/*used to check return codes from database*/
sqlite3 *db; /*database object*/

pthread_mutex_t mux_database;
pthread_mutex_t mux_local;
//mutex to protect access to database

//TODO mutex to copy local variable
struct sockaddr_in server_addr, client_addr;
int sd;
//char buffer[256];
int  val, size;




/* executed by each thread to process the request*/
void process_request(int * sc){
	int s_local = *sc;
	pthread_mutex_unlock(&mux_local);

	int rc;	/*used to check return codes from database*/

	char code = '5'; /*code returned to client*/
	char user_name[256];
	char operation[256];

	readLine(s_local,operation,sizeof(operation));
	readLine(s_local,user_name,sizeof(user_name));

	puts("All data read, inserting in database");
	if(strcmp(operation, "REGISTER") == 0){
		pthread_mutex_lock(&mux_database);
		code = my_register(user_name);
		pthread_mutex_unlock(&mux_database);
		enviar(s_local,&code,sizeof(code));
		close(s_local);

   	}else if(strcmp(operation, "UNREGISTER") == 0){
		pthread_mutex_lock(&mux_database);
		code = unregister(user_name);
		pthread_mutex_unlock(&mux_database);
		enviar(s_local,&code,sizeof(code));
		close(s_local);
	}else if(strcmp(operation, "PUBLISH") == 0){
		char file_name[256];
		char file_description[256];
		readLine(s_local,file_name,sizeof(file_name));
		readLine(s_local,file_description,sizeof(file_description));
		pthread_mutex_lock(&mux_database);
		code = publish(user_name, file_name, file_description);
		pthread_mutex_unlock(&mux_database);
		enviar(s_local,&code,sizeof(code));
		close(s_local);
	}else if(strcmp(operation, "DELETE") == 0){
		char file_name[256];
		readLine(s_local,file_name,sizeof(file_name));
		pthread_mutex_lock(&mux_database);
		code = my_delete(user_name, file_name);
		pthread_mutex_unlock(&mux_database);
		enviar(s_local,&code,sizeof(code));
		close(s_local);
	}else if(strcmp(operation, "LIST_USERS") == 0){
		struct ruser ans = malloc(sizeof(struct ruser));
		pthread_mutex_lock(&mux_database);
		list_users(user_name);
		pthread_mutex_unlock(&mux_database);

		enviar(s_local, &code, sizeof(code));
		close(s_local);
	}else if(strcmp(operation, "LIST_CONTENT") == 0){
		char user_content[256];
		readLine(s_local,user_content, sizeof(user_content));
		pthread_mutex_lock(&mux_database);
		struct rcontent res = list_content(user_content);
		pthread_mutex_unlock(&mux_database);
		close(s_local);

	}else if(strcmp(operation, "CONNECT") == 0){
		char client_port[8];
		readLine(s_local,client_port,sizeof(client_port));
		struct sockaddr_in addr;
   		socklen_t addr_size = sizeof(struct sockaddr_in);
    	getpeername(s_local, (struct sockaddr *)&addr, &addr_size);
   		char clientip[20];
    	strcpy(clientip, inet_ntoa(addr.sin_addr));
		puts(clientip);
		pthread_mutex_lock(&mux_database);
		code = my_connect(user_name,clientip,client_port);
		pthread_mutex_unlock(&mux_database);
		enviar(s_local,&code,sizeof(code));
		close(s_local);
	}else if(strcmp(operation, "DISCONNECT") == 0){
		pthread_mutex_lock(&mux_database);
		code = disconnect(user_name);
		pthread_mutex_unlock(&mux_database);
		enviar(s_local,&code,sizeof(code));
		close(s_local);   
	}

}
void print_usage() {
	    printf("Usage: server -p puerto \n");
}


void init_server(char * port_string){
	char host[]="0.0.0.0";
	struct hostent *hp;
	int port = atoi(port_string);
	sd= socket(AF_INET,SOCK_STREAM,IPPROTO_TCP);

	val = 1;
	setsockopt(sd,SOL_SOCKET, SO_REUSEADDR, (char*) &val, sizeof(int));

	bzero((char *)&server_addr, sizeof(server_addr));

	hp= (struct hostent*)gethostbyname(host);
    memcpy(&(server_addr.sin_addr), hp->h_addr, hp->h_length);
	server_addr.sin_family = AF_INET;
	server_addr.sin_port = htons(port);

	bind(sd, (struct sockaddr *) &server_addr , sizeof(server_addr));
	listen(sd,5);
}


int main(int argc, char *argv[]) {
	int  option = 0;
	char port[256]= "";

	while ((option = getopt(argc, argv,"p:")) != -1) {
		switch (option) {
		    	case 'p' : 
				strcpy(port, optarg);
		    		break;
		    	default: 
				print_usage(); 
		    		exit(-1);
		    }
	}
	if (strcmp(port,"")==0){
		print_usage();
		sqlite3_close(db);
		exit(-1);
	}
	init_database(db);
	init_server(port);
	printf("s> init server 127.0.0.1:%s\n", port);

	/*init mutex*/
	pthread_mutex_init(&mux_database,NULL);
	pthread_mutex_init(&mux_local,NULL);

	/*thread structures*/
	pthread_attr_t t_attr;
	pthread_t thid;
	pthread_attr_init(&t_attr);
	pthread_attr_setdetachstate(&t_attr,PTHREAD_CREATE_DETACHED);

	int sc; /*file descriptor of the accepted request*/
	while(1){
		size = sizeof(client_addr);

		pthread_mutex_lock(&mux_local);
		sc = accept(sd, (struct sockaddr *) &client_addr,(socklen_t * restrict) &size);
		/*When new request comes we create a thread to process it*/
		pthread_create(&thid, &t_attr, (void *)process_request, &sc);
	}
	
	sqlite3_close(db);
	return 0;
}