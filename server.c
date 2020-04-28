#include <unistd.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>
#include <netdb.h>
#include <pthread.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <sqlite3.h> /*libsqlite3-dev*/
#include "lines.h"

#define h_addr h_addr_list[0]
/*GLOBAL VARIABLES*/
//int rc;	/*used to check return codes from database*/
sqlite3 *db; /*database object*/

//TODO put mutex database
//TODO change the ip to connect not just from localhost
//TODO put mutex to protect s_local
struct sockaddr_in server_addr, client_addr;
int sd;
//char buffer[256];
int  val, size;


/* CALLBACK function from the count query*/
int count_rows(void *count, int argc, char **argv, char **azColName){
	int *count_int= count;
	*count_int= atoi(argv[0]);
	
	return 0;
}
/* executed by each thread to process the request*/
void process_request(int * sc){
	int rc;	/*used to check return codes from database*/
	int s_local = *sc;

	char user_name[256];
	char operation[256];

	recibir(s_local,operation,sizeof(operation));
	recibir(s_local,user_name,sizeof(user_name));

	puts("All data read, inserting in database");
	if(strcmp(operation, "REGISTER") == 0){
		char *zErrMsg = 0;
		/* We use this method instead of sprintf() to avoid sqlInjection*/
		char *query= sqlite3_mprintf("INSERT INTO USERS(USER_NAME) VALUES('%q');", user_name);
		rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
		if( rc != SQLITE_OK ){
			if(strcmp(zErrMsg,"SQL error: UNIQUE constraint failed: USERS.USER_NAME")){
				/*If we are here in means that user is already in database so in cannot connect again*/
				/*send the response to the client a close this socket*/
			}else{
				fprintf(stderr, "SQL error: %s\n", zErrMsg);
			}
     	
        sqlite3_free(zErrMsg);
   		}

		/* If there are not errors we create one table for the user*/
		query= sqlite3_mprintf("CREATE TABLE %q("  \
      	"FILE_NAME TEXT PRIMARY KEY   NOT NULL,"\
		"FILE_DESCRIPTION TEXT NOT NULL);", user_name);

		rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
		if( rc != SQLITE_OK ){
     		fprintf(stderr, "SQL error: %s\n", zErrMsg);
      		sqlite3_free(zErrMsg);
		}

   	}else if(strcmp(operation, "UNREGISTER") == 0){
		char *zErrMsg = 0;
		/* We use this method instead of sprintf() to avoid sqlInjection*/
		char *query= sqlite3_mprintf("DELETE FROM USERS WHERE USER_NAME='%q';", user_name);
		rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
		if( rc != SQLITE_OK ){
			if(strcmp(zErrMsg,"")){  //TODO check message
				/*If we are here in means that user is not in the database*/
				/*send the response to the client a close this socket*/
			}else{
				fprintf(stderr, "SQL error: %s\n", zErrMsg);
			}
     	
        sqlite3_free(zErrMsg);
   		}
  
		/* We delete the table*/
		query= sqlite3_mprintf("DROP TABLE %q;", user_name);

		rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
		if( rc != SQLITE_OK ){
     		fprintf(stderr, "SQL error: %s\n", zErrMsg);
      		sqlite3_free(zErrMsg);
		}



	}else if(strcmp(operation, "PUBLISH") == 0){
		char file_name[256];
		char file_description[256];

		recibir(s_local,file_name,sizeof(file_name));
		recibir(s_local,file_description,sizeof(file_description));

		char *zErrMsg = 0;

		char *query= sqlite3_mprintf("INSERT INTO %q(FILE_NAME, FILE_DESCRIPTION) VALUES(%q, %q);",user_name, file_name, file_description);

		rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
		if( rc != SQLITE_OK ){
			if(strcmp(zErrMsg,"SQL error: UNIQUE constraint failed: USERS.USER_NAME")){
				/*If we are here in means that user is already in database so in cannot connect again*/
				/*send the response to the client a close this socket*/
			}else{
				fprintf(stderr, "SQL error: %s\n", zErrMsg);
			}
     	
        sqlite3_free(zErrMsg);
   		}


	}else if(strcmp(operation, "DELETE") == 0){
		char file_name[256];

		recibir(s_local,file_name,sizeof(file_name));

		char *zErrMsg = 0;

		char *query= sqlite3_mprintf("DELETE FROM %q WHERE FILE_NAME=%q;",user_name, file_name);
		
		rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
		if( rc != SQLITE_OK ){
			if(strcmp(zErrMsg,"SQL error: UNIQUE constraint failed: USERS.USER_NAME")){
				/*If we are here in means that user is already in database so in cannot connect again*/
				/*send the response to the client a close this socket*/
			}else{
				fprintf(stderr, "SQL error: %s\n", zErrMsg);
			}
     	
        sqlite3_free(zErrMsg);
   		}

	}else if(strcmp(operation, "LIST_USERS") == 0){
		char *zErrMsg = 0;
		int num_users=0;
		/*FIRST WE RETRIEVE THE NUMBER OF THE USERS WITH COUNT*/
		char * query= "SELECT COUNT(*) FROM USERS";

		rc = sqlite3_exec(db, query, count_rows, &num_users, &zErrMsg);
		if( rc != SQLITE_OK ){
				fprintf(stderr, "SQL error: %s\n", zErrMsg);
		}

		sqlite3_stmt *res;
		
		char *sql = "SELECT * FROM USERS";
    	rc = sqlite3_prepare_v2(db, sql, -1, &res, 0);
        if(rc != SQLITE_OK){
        	fprintf(stderr, "Failed to execute statement: %s\n", sqlite3_errmsg(db));
		}
			//TODO send number of rows
   		int step = sqlite3_step(res);
    	if (step == SQLITE_ROW) {
			//TODO send back responses
		}

	}else if(strcmp(operation, "LIST_CONTENT") == 0){
		char *zErrMsg = 0;
		char user_content[256];
		recibir(s_local,user_content, sizeof(user_content));

		int num_content=0;
		char * query= sqlite3_mprintf("SELECT COUNT(*) FROM %q",user_content);
		//TODO check that the user exists

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
    
   		int step = sqlite3_step(res);
    	if (step == SQLITE_ROW) {
			//TODO send back responses
		}

	}else if(strcmp(operation, "CONNECT") == 0){
		char client_port[8];
		recibir(s_local,client_port,sizeof(client_port));

		char *zErrMsg = 0;
		/* We use this method instead of sprintf() to avoid sqlInjection*/
		char *query= sqlite3_mprintf("UPDATE USERS SET PORT = %q WHERE USER_NAME = %q",client_port,user_name);
		rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
		if( rc != SQLITE_OK ){
			if(strcmp(zErrMsg,"")){ //TODO Check that the error code is in case it doesn't find it 
				/*If we are here in means that user is already in database so in cannot connect again*/
				/*send the response to the client a close this socket*/
			}else{
				fprintf(stderr, "SQL error: %s\n", zErrMsg);
			}
     	
        sqlite3_free(zErrMsg);
   		}

	}else if(strcmp(operation, "DISCONNECT") == 0){
		char *zErrMsg = 0;
		/* We use this method instead of sprintf() to avoid sqlInjection*/
		char *query= sqlite3_mprintf("UPDATE USERS SET PORT = NULL WHERE USER_NAME = %q",user_name);
		rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
		if( rc != SQLITE_OK ){
			if(strcmp(zErrMsg,"")){ //TODO Check that the error code is in case it doesn't find it 
				/*If we are here in means that user is already in database so in cannot connect again*/
				/*send the response to the client a close this socket*/
			}else{
				fprintf(stderr, "SQL error: %s\n", zErrMsg);
			}
     	
        sqlite3_free(zErrMsg);
   		}
	}else{

	}


	close(s_local);
		
}
void print_usage() {
	    printf("Usage: server -p puerto \n");
}


void init_server(char * port_string){
	char host[]="localhost";
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

	
		/*Create new thread to process the message*/
	}

	//  INSERT SERVER CODE HERE
	
	sqlite3_close(db);
	return 0;
}