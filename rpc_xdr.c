/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#include "rpc.h"

bool_t
xdr_user (XDR *xdrs, user *objp)
{
	register int32_t *buf;

	 if (!xdr_string (xdrs, &objp->name, 256))
		 return FALSE;
	 if (!xdr_string (xdrs, &objp->ip, 20))
		 return FALSE;
	 if (!xdr_string (xdrs, &objp->port, 8))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_user (XDR *xdrs, user *objp)
{
	register int32_t *buf;

	 if (!xdr_array (xdrs, (char **)&objp->user_val, (u_int *) &objp->user_len, ~0,
		sizeof (luser), (xdrproc_t) xdr_luser))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_ruser (XDR *xdrs, ruser *objp)
{
	register int32_t *buf;

	 if (!xdr_int (xdrs, &objp->err))
		 return FALSE;
	 if (!xdr_luser (xdrs, &objp->l))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_content (XDR *xdrs, content *objp)
{
	register int32_t *buf;

	 if (!xdr_string (xdrs, &objp->fileName, 256))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_content (XDR *xdrs, content *objp)
{
	register int32_t *buf;

	 if (!xdr_array (xdrs, (char **)&objp->content_val, (u_int *) &objp->content_len, ~0,
		sizeof (lcontent), (xdrproc_t) xdr_lcontent))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_rcontent (XDR *xdrs, rcontent *objp)
{
	register int32_t *buf;

	 if (!xdr_int (xdrs, &objp->err))
		 return FALSE;
	 if (!xdr_lcontent (xdrs, &objp->l))
		 return FALSE;
	return TRUE;
}

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
