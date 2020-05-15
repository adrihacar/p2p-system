package upper;

import javax.xml.ws.Endpoint;


class UpperPublisher {
    public static void main(String[ ] args) {
        final String url = "http://localhost:8888/rs";
        System.out.println("Publishing UpperService at endpoint" + url);
        Endpoint.publish(url, new UpperService());
    }
}


/*
esto para la descripion en el el metodo para publicar archivos de client.java


*/

