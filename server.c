#include <unistd.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>
#include <sqlite3.h> /*libsqlite3-dev*/

/*GLOBAL VARIABLES*/
int rc;	/*used to check return codes from database*/
sqlite3 *db; /*database object*/

void print_usage() {
	    printf("Usage: server -p puerto \n");
}

void init_database(){
	char *zErrMsg = 0;
	char * init_query="CREATE TABLE USERS("  \
      "USER_NAME TEXT PRIMARY KEY     NOT NULL);";

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
	init_database();

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
	printf("Port: %s\n", port);


	//  INSERT SERVER CODE HERE
	
	sqlite3_close(db);
	return 0;
}
	
