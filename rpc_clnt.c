/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#include <memory.h> /* for memset */
#include "rpc.h"

/* Default timeout can be changed using clnt_control() */
static struct timeval TIMEOUT = { 25, 0 };

enum clnt_stat 
init_database_1(void *clnt_res, CLIENT *clnt)
{
	 return (clnt_call (clnt, init_database, (xdrproc_t) xdr_void, (caddr_t) NULL,
		(xdrproc_t) xdr_void, (caddr_t) clnt_res,
		TIMEOUT));

}

enum clnt_stat 
my_register_1(char *user_name, char *clnt_res,  CLIENT *clnt)
{
	return (clnt_call(clnt, my_register,
		(xdrproc_t) xdr_wrapstring, (caddr_t) &user_name,
		(xdrproc_t) xdr_char, (caddr_t) clnt_res,
		TIMEOUT));
}

enum clnt_stat 
unregister_1(char *user_name, char *clnt_res,  CLIENT *clnt)
{
	return (clnt_call(clnt, unregister,
		(xdrproc_t) xdr_wrapstring, (caddr_t) &user_name,
		(xdrproc_t) xdr_char, (caddr_t) clnt_res,
		TIMEOUT));
}

enum clnt_stat 
publish_1(char *user_name, char *file_name, char *file_description, char *clnt_res,  CLIENT *clnt)
{
	publish_1_argument arg;
	arg.user_name = user_name;
	arg.file_name = file_name;
	arg.file_description = file_description;
	return (clnt_call (clnt, publish, (xdrproc_t) xdr_publish_1_argument, (caddr_t) &arg,
		(xdrproc_t) xdr_char, (caddr_t) clnt_res,
		TIMEOUT));
}

enum clnt_stat 
my_delete_1(char *user_name, char *file_name, char *clnt_res,  CLIENT *clnt)
{
	my_delete_1_argument arg;
	arg.user_name = user_name;
	arg.file_name = file_name;
	return (clnt_call (clnt, my_delete, (xdrproc_t) xdr_my_delete_1_argument, (caddr_t) &arg,
		(xdrproc_t) xdr_char, (caddr_t) clnt_res,
		TIMEOUT));
}

enum clnt_stat 
my_connect_1(char *user_name, char *clientip, char *client_port, char *clnt_res,  CLIENT *clnt)
{
	my_connect_1_argument arg;
	arg.user_name = user_name;
	arg.clientip = clientip;
	arg.client_port = client_port;
	return (clnt_call (clnt, my_connect, (xdrproc_t) xdr_my_connect_1_argument, (caddr_t) &arg,
		(xdrproc_t) xdr_char, (caddr_t) clnt_res,
		TIMEOUT));
}

enum clnt_stat 
disconnect_1(char *user_name, char *clnt_res,  CLIENT *clnt)
{
	return (clnt_call(clnt, disconnect,
		(xdrproc_t) xdr_wrapstring, (caddr_t) &user_name,
		(xdrproc_t) xdr_char, (caddr_t) clnt_res,
		TIMEOUT));
}

enum clnt_stat 
list_users_1(char *user_name, ruser *clnt_res,  CLIENT *clnt)
{
	return (clnt_call(clnt, list_users,
		(xdrproc_t) xdr_wrapstring, (caddr_t) &user_name,
		(xdrproc_t) xdr_ruser, (caddr_t) clnt_res,
		TIMEOUT));
}

enum clnt_stat 
list_content_1(char *user_name, rcontent *clnt_res,  CLIENT *clnt)
{
	return (clnt_call(clnt, list_content,
		(xdrproc_t) xdr_wrapstring, (caddr_t) &user_name,
		(xdrproc_t) xdr_rcontent, (caddr_t) clnt_res,
		TIMEOUT));
}
