#include <stdio.h>
#include <sqlite3.h>
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
#include "myrpc.h"

/* Check if user exist */
int user_exists(sqlite3 *db, char *user){
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
int user_connected(sqlite3 *db, char *user){
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
int file_exists(sqlite3 *db, char *file, char *user){
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
int count_rows(sqlite3 *db, void *count, int argc, char **argv, char **azColName){
	int *count_int= count;
	*count_int= atoi(argv[0]);
	
	return 0;
}

void init_database(){
	int rc;
	char *zErrMsg = 0;
	sqlite3 *db;
	char * init_query="CREATE TABLE USERS("  \
      "USER_NAME TEXT PRIMARY KEY     NOT NULL," \
	  "IP    TEXT," \
	  "PORT TEXT);";

	remove("database.db");
	rc = sqlite3_open("database.db", &db);
   	if(rc) {
   	   fprintf(stderr, "Can't open database: %s\n", sqlite3_errmsg(db));
   	   return;
   	}

	rc = sqlite3_exec(db, init_query, NULL, 0, &zErrMsg);
	if( rc != SQLITE_OK ){
      fprintf(stderr, "SQL error: %s\n", zErrMsg);
      sqlite3_free(zErrMsg);
   	}
	//rc = sqlite3_close(db);
   	//if(rc != SQLITE_OK) {
   	//	fprintf(stderr, "Can't close database: %s\n", sqlite3_errmsg(db));
   	//}
}


char my_register(char * user_name){
	int rc;
	sqlite3 *db;
	rc = sqlite3_open("database.db", &db);
   	if(rc) {
   	   fprintf(stderr, "Can't open database: %s\n", sqlite3_errmsg(db));
   	   return '2';
   	}
	char code = '2';
	char *zErrMsg = 0;
	/* We use this method instead of sprintf() to avoid sqlInjection*/
	char *query= sqlite3_mprintf("INSERT INTO USERS(USER_NAME) VALUES('%q');", user_name);
	rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
	if( rc != SQLITE_OK ){
		if(strstr(zErrMsg,"SQL error: UNIQUE constraint failed: USERS.USER_NAME") == 0){
			/*If we are here in means that user is already in database so in cannot connect again*/
			code = '1';
		}else{
			code = '2';
			fprintf(stderr, "SQL error: %s\n", zErrMsg);
		}
    	//rc = sqlite3_close(db);
   		//if(rc != SQLITE_OK) {
   		//	fprintf(stderr, "Can't close database: %s\n", sqlite3_errmsg(db));
   		//}
		return code;
   	}
	/* If there are not errors we create one table for the user*/
	query= sqlite3_mprintf("CREATE TABLE %q("  \
    "FILE_NAME TEXT PRIMARY KEY   NOT NULL,"\
	"FILE_DESCRIPTION TEXT NOT NULL);", user_name);
	rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
	if( rc != SQLITE_OK ){
     	fprintf(stderr, "SQL error: %s\n", zErrMsg);
      	sqlite3_free(zErrMsg);
		code = '2';
		fprintf(stderr, "SQL error: %s\n", zErrMsg);
		//rc = sqlite3_close(db);
   		//if(rc != SQLITE_OK) {
   		//	fprintf(stderr, "Can't close database: %s\n", sqlite3_errmsg(db));
   		//}
		return code;
	}
	//rc = sqlite3_close(db);
   	//if(rc != SQLITE_OK) {
   	//	fprintf(stderr, "Can't close database: %s\n", sqlite3_errmsg(db));
   	//}
	return '0';
}

char unregister(char * user_name){
	int rc;
	sqlite3 *db;
	rc = sqlite3_open("database.db", &db);
   	if(rc) {
   	   fprintf(stderr, "Can't open database: %s\n", sqlite3_errmsg(db));
   	   return '2';
   	}
	char code = '2';
	char *zErrMsg = 0;
	/* We use this method instead of sprintf() to avoid sqlInjection*/
	char * query= sqlite3_mprintf("DROP TABLE %q;", user_name);
	/* We delete the table*/
	rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
	if( rc != SQLITE_OK ){
		if(strstr(zErrMsg, "no such table")){ 
			code = '1';
		}else{
			fprintf(stderr, "SQL error: %s\n", zErrMsg);
			code = '2';
		}
    	sqlite3_free(zErrMsg);
		//rc = sqlite3_close(db);
   		//if(rc != SQLITE_OK) {
   		//	fprintf(stderr, "Can't close database: %s\n", sqlite3_errmsg(db));
   		//}
		return code;
   	}
	query= sqlite3_mprintf("DELETE FROM USERS WHERE USER_NAME='%q';", user_name);
	rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
	if( rc != SQLITE_OK ){
    	sqlite3_free(zErrMsg);
		code = '2';
		fprintf(stderr, "SQL error: %s\n", zErrMsg);
		//rc = sqlite3_close(db);
		return code;
	}
	//rc = sqlite3_close(db);
   	//if(rc != SQLITE_OK) {
   	//	fprintf(stderr, "Can't close database: %s\n", sqlite3_errmsg(db));
   	//}
	return '0';
}

char publish(char * user_name, char * file_name, char * file_description){
	int rc;
	sqlite3 *db;
	rc = sqlite3_open("database.db", &db);
   	if(rc) {
   	   fprintf(stderr, "Can't open database: %s\n", sqlite3_errmsg(db));
   	   return '4';
   	}
	char code = '4';
	char *zErrMsg = 0;
	if(user_exists(db, user_name) == 0){
		code = '1';
	}else if(user_connected(db, user_name) == 0){
		code = '2';
	}else if(file_exists(db, file_name,user_name) == 1){
		code = '3';
	}else{
		char *query= sqlite3_mprintf("INSERT INTO %q(FILE_NAME, FILE_DESCRIPTION) VALUES('%q', '%q');",user_name, file_name, file_description);
		rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
		if( rc != SQLITE_OK ){
			fprintf(stderr, "SQL error: %s\n", zErrMsg);
    		sqlite3_free(zErrMsg);
			code = '4';
   		}else{
			code='0';
		}
	}
	//rc = sqlite3_close(db);
   	//if(rc != SQLITE_OK) {
   	//	fprintf(stderr, "Can't close database: %s\n", sqlite3_errmsg(db));
   	//}
	return code;
}

char my_delete(char * user_name, char * file_name){
	int rc;
	sqlite3 *db;
	rc = sqlite3_open("database.db", &db);
   	if(rc) {
   	   fprintf(stderr, "Can't open database: %s\n", sqlite3_errmsg(db));
   	   return '4';
   	}
	char code = '4';
	char *zErrMsg = 0;
	if(user_exists(db, user_name) == 0){
		code = '1';
	}else if(user_connected(db, user_name) == 0){
		code = '2';
	}else if(file_exists(db, file_name,user_name) == 0){
		code = '3';
	}else{
		char *query= sqlite3_mprintf("DELETE FROM %q WHERE FILE_NAME='%q';",user_name, file_name);
		rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
		if( rc != SQLITE_OK ){
			fprintf(stderr, "SQL error: %s\n", zErrMsg);
        	sqlite3_free(zErrMsg);
			code = '4';
   		}else{
			code = '0';
		}
	}
	//rc = sqlite3_close(db);
   	//if(rc != SQLITE_OK) {
   	//	fprintf(stderr, "Can't close database: %s\n", sqlite3_errmsg(db));
   	//}
	return code;
}

char my_connect (char * user_name, char * clientip, char * client_port){
	int rc;
	sqlite3 *db;
	rc = sqlite3_open("database.db", &db);
   	if(rc) {
   	   fprintf(stderr, "Can't open database: %s\n", sqlite3_errmsg(db));
   	   return '3';
   	}
	char code = '3';
	char *zErrMsg = 0;
	if(user_exists(db, user_name) == 0){
		code = '1';
	}else if(user_connected(db, user_name) == 1){
		code = '2';
	}else{
		/* We use this method instead of sprintf() to avoid sqlInjection*/
		char *query= sqlite3_mprintf("UPDATE USERS SET PORT = '%q', IP = '%q'  WHERE USER_NAME = '%q'",client_port,clientip,user_name);
		puts(query);
		rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
		if( rc != SQLITE_OK ){
			fprintf(stderr, "SQL error: %s\n", zErrMsg);
    		sqlite3_free(zErrMsg);
			code = '3';
		}else{
			code = '0';
		}
	}
	//rc = sqlite3_close(db);
   	//if(rc != SQLITE_OK) {
   	//	fprintf(stderr, "Can't close database: %s\n", sqlite3_errmsg(db));
   	//}
	return code;
}

char disconnect (char * user_name){
	int rc;
	sqlite3 *db;
	rc = sqlite3_open("database.db", &db);
   	if(rc) {
   	   fprintf(stderr, "Can't open database: %s\n", sqlite3_errmsg(db));
   	   return '3';
   	}
	char code = '3';
	char *zErrMsg = 0;
	if(user_exists(db, user_name) == 0){
		code = '1';
	}else if(user_connected(db, user_name) == 0){
		code = '2';
	}else{
		/* We use this method instead of sprintf() to avoid sqlInjection*/
		char *query= sqlite3_mprintf("UPDATE USERS SET PORT = NULL WHERE USER_NAME = '%q'",user_name);
		rc = sqlite3_exec(db, query, NULL, 0, &zErrMsg);
		if( rc != SQLITE_OK ){
			fprintf(stderr, "SQL error: %s\n", zErrMsg);
			sqlite3_free(zErrMsg);
			code = '3';
		}else{
			code='0';
		}	
   	}
	//rc = sqlite3_close(db);
   	//if(rc != SQLITE_OK) {
   	//	fprintf(stderr, "Can't close database: %s\n", sqlite3_errmsg(db));
   	//}
	return code;
}