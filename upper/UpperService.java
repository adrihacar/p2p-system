package upper;

import javax.jws.WebService;
import javax.jws.WebMethod;


@WebService
public class UpperService {
    @WebMethod
    public String toUpperCase(String str){
        return str.toUpperCase();
    }
}