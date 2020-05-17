/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#include "rpc.h"

bool_t
xdr_publish_1_argument (XDR *xdrs, publish_1_argument *objp)
{
	 if (!xdr_string (xdrs, &objp->user_name, 256))
		 return FALSE;
	 if (!xdr_string (xdrs, &objp->file_name, 256))
		 return FALSE;
	 if (!xdr_string (xdrs, &objp->file_description, 256))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_my_delete_1_argument (XDR *xdrs, my_delete_1_argument *objp)
{
	 if (!xdr_string (xdrs, &objp->user_name, 256))
		 return FALSE;
	 if (!xdr_string (xdrs, &objp->file_name, 256))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_my_connect_1_argument (XDR *xdrs, my_connect_1_argument *objp)
{
	 if (!xdr_string (xdrs, &objp->user_name, 256))
		 return FALSE;
	 if (!xdr_string (xdrs, &objp->clientip, 20))
		 return FALSE;
	 if (!xdr_string (xdrs, &objp->client_port, 8))
		 return FALSE;
	return TRUE;
}