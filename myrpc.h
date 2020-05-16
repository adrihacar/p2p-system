
// auxiliary functions

int user_exists(sqlite3 *db, char *user);

int user_connected(sqlite3 *db, char *user);

int file_exists(sqlite3 *db, char *file, char *user);

int count_rows(sqlite3 *db, void *count, int argc, char **argv, char **azColName);

// create database

void init_database(sqlite3 *db);

// Communication with database

char my_register(sqlite3 *db, char * user_name);

char unregister(sqlite3 *db, char * user_name);

char publish(sqlite3 *db, char * user_name, char * file_name, char * file_description);

char my_delete(sqlite3 *db, char * user_name, char * file_name);

char my_connect (sqlite3 *db, char * user_name, char * clientip, char * client_port);

char disconnect (sqlite3 *db, char * user_name);


