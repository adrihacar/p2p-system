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
#include <rpc/rpc.h>

#define h_addr h_addr_list[0]
/*GLOBAL VARIABLES*/
//int rc;	/*used to check return codes from database*/
sqlite3 *db; /*database object*/

pthread_mutex_t mux_database;
//mutex to protect access to database

//TODO mutex to copy local variable
struct sockaddr_in server_addr, client_addr;
int sd;
//char buffer[256];
int  val, size;


/* Check if user exist */
int user_exists(char *user){
	int res=1; /* 1 true, 0 false*/
	char *zErrMsg = 0;
	int rc;
	/* We use this method instead of sprintf() to avoid sqlInjection*/
	char *query= sqlite3_mprintf("SELECT * FROM %q", user);

	rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
	if( rc != SQLITE_OK ){
		res=0;
	}

	return res;
}

/*check if user is connected */
int user_connected(char *user){
	int result=0; /* 1 true, 0 false*/
	int rc;
	
	sqlite3_stmt *res;

	char *sql = sqlite3_mprintf("SELECT * FROM USERS WHERE USER_NAME='%q' AND PORT IS NOT NULL", user);
    rc = sqlite3_prepare_v2(db, sql, -1, &res, 0);
    if(rc != SQLITE_OK){
       	fprintf(stderr, "Failed to execute statement: %s\n", sqlite3_errmsg(db));
	}
   	int step = sqlite3_step(res);
    if (step == SQLITE_ROW) {
		result = 1;
	}

	return result;
}
int file_exists(char *file, char *user){
	int result=0; /* 1 true, 0 false*/
	int rc;
	
	sqlite3_stmt *res;

	char *sql = sqlite3_mprintf("SELECT * FROM %q WHERE FILE_NAME = '%q'", user, file);
    rc = sqlite3_prepare_v2(db, sql, -1, &res, 0);
    if(rc != SQLITE_OK){
       	fprintf(stderr, "Failed to execute statement: %s\n", sqlite3_errmsg(db));
	}
   	int step = sqlite3_step(res);
    if (step == SQLITE_ROW) {
		result = 1;
	}

	return result;
}


/* CALLBACK function from the count query*/
int count_rows(void *count, int argc, char **argv, char **azColName){
	int *count_int= count;
	*count_int= atoi(argv[0]);
	
	return 0;
}

/// EXTRACT functions

char regist(int s_local,char * user_name){
	int rc;
	char code = '5';
	char *zErrMsg = 0;
	/* We use this method instead of sprintf() to avoid sqlInjection*/
	char *query= sqlite3_mprintf("INSERT INTO USERS(USER_NAME) VALUES('%q');", user_name);

	pthread_mutex_lock(&mux_database);
	rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);

	if( rc != SQLITE_OK ){
		if(strstr(zErrMsg,"SQL error: UNIQUE constraint failed: USERS.USER_NAME") == 0){
			/*If we are here in means that user is already in database so in cannot connect again*/
			/*send the response to the client a close this socket*/
			code='1';
				
		}else{
			code='2';
			fprintf(stderr, "SQL error: %s\n", zErrMsg);
		}
		pthread_mutex_unlock(&mux_database);
		enviar(s_local,&code,sizeof(code));
		close(s_local);
    	sqlite3_free(zErrMsg);
	}
	/* If there are not errors we create one table for the user*/
	query= sqlite3_mprintf("CREATE TABLE %q("  \
  	"FILE_NAME TEXT PRIMARY KEY   NOT NULL,"\
	"FILE_DESCRIPTION TEXT NOT NULL);", user_name);
	rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
	pthread_mutex_unlock(&mux_database);
	if( rc != SQLITE_OK ){
    	fprintf(stderr, "SQL error: %s\n", zErrMsg);
    	sqlite3_free(zErrMsg);
		code='2';
		fprintf(stderr, "SQL error: %s\n", zErrMsg);
		return code;
	}
	return '0';
}

void unregister (int s_local,char* user_name){
	int rc;
	char code = '5';
	char *zErrMsg = 0;
	/* We use this method instead of sprintf() to avoid sqlInjection*/
	char * query= sqlite3_mprintf("DROP TABLE %q;", user_name);
	/* We delete the table*/
	pthread_mutex_lock(&mux_database);
	rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
	if( rc != SQLITE_OK ){
		if(strstr(zErrMsg, "no such table")){ 
			code ='1';
		}else{
			fprintf(stderr, "SQL error: %s\n", zErrMsg);
			code ='2';
		}
	pthread_mutex_unlock(&mux_database);
    sqlite3_free(zErrMsg);
	enviar(s_local,&code,sizeof(code));
	/*send the response to the client a close this socket*/
	close(s_local);
	pthread_exit(NULL);
	}
	query= sqlite3_mprintf("DELETE FROM USERS WHERE USER_NAME='%q';", user_name);
	rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
	pthread_mutex_unlock(&mux_database);
	if( rc != SQLITE_OK ){
    	sqlite3_free(zErrMsg);
		code='2';
		enviar(s_local,&code,sizeof(code));
		close(s_local);
		fprintf(stderr, "SQL error: %s\n", zErrMsg);
		pthread_exit(NULL);
	}
	code='0';
	enviar(s_local,&code,sizeof(code));
	close(s_local);
}


/* executed by each thread to process the request*/
void process_request(int * sc){
	int rc;	/*used to check return codes from database*/
	int s_local = *sc;

	char code = '5'; /*code returned to client*/
	char user_name[256];
	char operation[256];

	readLine(s_local,operation,sizeof(operation));
	readLine(s_local,user_name,sizeof(user_name));

	puts("All data read, inserting in database");
	if(strcmp(operation, "REGISTER") == 0){
		code = regist(s_local, user_name);
		enviar(s_local,&code,sizeof(code));
		close(s_local);

   	}else if(strcmp(operation, "UNREGISTER") == 0){
		unregister(s_local,user_name);
	}else if(strcmp(operation, "PUBLISH") == 0){
		code ='4';
		char file_name[256];
		char file_description[256];

		readLine(s_local,file_name,sizeof(file_name));
		readLine(s_local,file_description,sizeof(file_description));

		char *zErrMsg = 0;

		pthread_mutex_lock(&mux_database);

		if(user_exists(user_name) == 0){
			code='1';
		}if(user_connected(user_name) == 0){
			code='2';
		}else if(file_exists(file_name,user_name) == 1){
			code='3';
		}else{
			char *query= sqlite3_mprintf("INSERT INTO %q(FILE_NAME, FILE_DESCRIPTION) VALUES('%q', '%q');",user_name, file_name, file_description);

			rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);

			if( rc != SQLITE_OK ){
				fprintf(stderr, "SQL error: %s\n", zErrMsg);
        		sqlite3_free(zErrMsg);
   			}else{
				code='0';
			}
		}
		pthread_mutex_unlock(&mux_database);
		enviar(s_local,&code,sizeof(code));
		close(s_local);


	}else if(strcmp(operation, "DELETE") == 0){
		code ='4';
		char file_name[256];

		readLine(s_local,file_name,sizeof(file_name));

		char *zErrMsg = 0;

		pthread_mutex_lock(&mux_database);

		if(user_exists(user_name) == 0){
			code='1';
		}else if(user_connected(user_name) == 0){
			code='2';

		}else if(file_exists(file_name,user_name) == 0){
			code='3';

		}else{


		char *query= sqlite3_mprintf("DELETE FROM %q WHERE FILE_NAME='%q';",user_name, file_name);
		
		rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
		if( rc != SQLITE_OK ){
			fprintf(stderr, "SQL error: %s\n", zErrMsg);
        	sqlite3_free(zErrMsg);
   		}else{
			 code='0';
		   }
		}
		pthread_mutex_unlock(&mux_database);
		enviar(s_local,&code,sizeof(code));
		close(s_local);


	}else if(strcmp(operation, "LIST_USERS") == 0){
		char *zErrMsg = 0;
		int num_users=0;
		char buf[256];

		pthread_mutex_lock(&mux_database);

		if(user_exists(user_name) == 0){
			code='1';
			pthread_mutex_unlock(&mux_database);
			enviar(s_local,&code,sizeof(code));
			close(s_local);
			pthread_exit(NULL);
		}if(user_connected(user_name) == 0){
			code='2';
			pthread_mutex_unlock(&mux_database);
			enviar(s_local,&code,sizeof(code));
			close(s_local);
			pthread_exit(NULL);
		}else{

		/*FIRST WE RETRIEVE THE NUMBER OF THE USERS WITH COUNT*/
		char * query= "SELECT COUNT(*) FROM USERS WHERE PORT IS NOT NULL";

		rc = sqlite3_exec(db, query, count_rows, &num_users, &zErrMsg);
		if( rc != SQLITE_OK ){
			code='3';
			enviar(s_local,&code,sizeof(code));
			close(s_local);
			fprintf(stderr, "SQL error: %s\n", zErrMsg);
			pthread_exit(NULL);
		}

		sqlite3_stmt *res;
		
		char *sql = "SELECT * FROM USERS WHERE PORT IS NOT NULL";
    	rc = sqlite3_prepare_v2(db, sql, -1, &res, 0);
        if(rc != SQLITE_OK){
			code='3';
			enviar(s_local,&code,sizeof(code));
			close(s_local);
			pthread_mutex_unlock(&mux_database);
        	fprintf(stderr, "Failed to execute statement: %s\n", sqlite3_errmsg(db));
			pthread_exit(NULL);
		}
			code='0';
			enviar(s_local,&code,sizeof(code));


			sprintf(buf, "%d", num_users);
			enviar(s_local,buf,strlen(buf)+1);

   		int step = sqlite3_step(res);
    	while (step == SQLITE_ROW) {

			sprintf(buf, "%s", sqlite3_column_text(res, 0));
			enviar(s_local,buf,strlen(buf)+1);

			sprintf(buf, "%s", sqlite3_column_text(res, 1));
			enviar(s_local,buf,strlen(buf)+1);

			sprintf(buf, "%s", sqlite3_column_text(res, 2));
			enviar(s_local,buf,strlen(buf)+1);

			step = sqlite3_step(res);
		}
		pthread_mutex_unlock(&mux_database);
		close(s_local);
		}

	}else if(strcmp(operation, "LIST_CONTENT") == 0){
		char buf[256];
		char *zErrMsg = 0;
		char user_content[256];


		readLine(s_local,user_content, sizeof(user_content));


		pthread_mutex_lock(&mux_database);


		if(user_exists(user_name) == 0){
			code='1';
			pthread_mutex_unlock(&mux_database);
			enviar(s_local,&code,sizeof(code));
			close(s_local);
			pthread_exit(NULL);
		}else if(user_connected(user_name) == 0){
			code='2';
			pthread_mutex_unlock(&mux_database);
			enviar(s_local,&code,sizeof(code));
			close(s_local);
			pthread_exit(NULL);
		}else if(user_exists(user_content) == 0){
			code='3';
			pthread_mutex_unlock(&mux_database);
			enviar(s_local,&code,sizeof(code));
			close(s_local);
			pthread_exit(NULL);

		}else{


		int num_content=0;
		char * query= sqlite3_mprintf("SELECT COUNT(*) FROM %q",user_content);

		rc = sqlite3_exec(db, query, count_rows, &num_content, &zErrMsg);
		if( rc != SQLITE_OK ){
				fprintf(stderr, "SQL error: %s\n", zErrMsg);
		}
		
		sqlite3_stmt *res;
		
		char *sql =sqlite3_mprintf("SELECT * FROM %q", user_content ) ;
    	rc = sqlite3_prepare_v2(db, sql, -1, &res, 0);
        if(rc != SQLITE_OK){
        	fprintf(stderr, "Failed to execute statement: %s\n", sqlite3_errmsg(db));
		}

		code='0';
		enviar(s_local,&code,sizeof(code));

		sprintf(buf, "%d", num_content);
		enviar(s_local,buf,strlen(buf)+1);
    
   		int step = sqlite3_step(res);
    	while(step == SQLITE_ROW) {
			sprintf(buf, "%s", sqlite3_column_text(res, 0));
			enviar(s_local,buf,strlen(buf)+1);
			step = sqlite3_step(res);
		}
		pthread_mutex_unlock(&mux_database);
		close(s_local);
		}

	}else if(strcmp(operation, "CONNECT") == 0){
		char client_port[8];
		readLine(s_local,client_port,sizeof(client_port));


		struct sockaddr_in addr;
   		socklen_t addr_size = sizeof(struct sockaddr_in);
    	getpeername(s_local, (struct sockaddr *)&addr, &addr_size);
   		char clientip[20];
    	strcpy(clientip, inet_ntoa(addr.sin_addr));
		puts(clientip);

		code='3';

		char *zErrMsg = 0;
		pthread_mutex_lock(&mux_database);

		if(user_exists(user_name) == 0){
			code='1';
		}else if(user_connected(user_name) == 1){
			code='2';
		}else{
			/* We use this method instead of sprintf() to avoid sqlInjection*/
			char *query= sqlite3_mprintf("UPDATE USERS SET PORT = '%q', IP = '%q'  WHERE USER_NAME = '%q'",client_port,clientip,user_name);
			puts(query);
			rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);

			if( rc != SQLITE_OK ){
				fprintf(stderr, "SQL error: %s\n", zErrMsg);
        		sqlite3_free(zErrMsg);
   			}else{
				   code='0';
			}

		}
		pthread_mutex_unlock(&mux_database);
		enviar(s_local,&code,sizeof(code));
		close(s_local);

	}else if(strcmp(operation, "DISCONNECT") == 0){
		code='3';
		char *zErrMsg = 0;

		pthread_mutex_lock(&mux_database);

		if(user_exists(user_name) == 0){
			code='1';
		}else if(user_connected(user_name) == 0){
			code='2';
		}else{

		/* We use this method instead of sprintf() to avoid sqlInjection*/
		char *query= sqlite3_mprintf("UPDATE USERS SET PORT = NULL WHERE USER_NAME = '%q'",user_name);
		rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
		if( rc != SQLITE_OK ){
			fprintf(stderr, "SQL error: %s\n", zErrMsg);
			sqlite3_free(zErrMsg);
		}else{
			code='0';
		}
     	
   		}
		pthread_mutex_unlock(&mux_database);
		enviar(s_local,&code,sizeof(code));
		close(s_local);   
	}
	pthread_mutex_unlock(&mux_database);

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
void init_database(){
	int rc;
	char *zErrMsg = 0;
	char * init_query="CREATE TABLE USERS("  \
      "USER_NAME TEXT PRIMARY KEY     NOT NULL," \
	  "IP    TEXT," \
	  "PORT TEXT);";

	remove("database.db");
	rc = sqlite3_open("database.db", &db);
   	if(rc) {
   	   fprintf(stderr, "Can't open database: %s\n", sqlite3_errmsg(db));
   	   exit(-2);
   	}

	rc = sqlite3_exec(db, init_query, NULL, 0, &zErrMsg);
	if( rc != SQLITE_OK ){
      fprintf(stderr, "SQL error: %s\n", zErrMsg);
      sqlite3_free(zErrMsg);
   	}
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
	init_database();
	init_server(port);
	printf("s> init server 127.0.0.1:%s\n", port);

	/*init mutex*/
	pthread_mutex_init(&mux_database,NULL);

	/*thread structures*/
	pthread_attr_t t_attr;
	pthread_t thid;
	pthread_attr_init(&t_attr);
	pthread_attr_setdetachstate(&t_attr,PTHREAD_CREATE_DETACHED);

	int sc; /*file descriptor of the accepted request*/
	while(1){
		size = sizeof(client_addr);
		sc = accept(sd, (struct sockaddr *) &client_addr,(socklen_t * restrict) &size);
		/*When new request comes we create a thread to process it*/
		pthread_create(&thid, &t_attr, (void *)process_request, &sc);
	}
	
	sqlite3_close(db);
	return 0;
}