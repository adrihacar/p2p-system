
package client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the client package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ToUpperCaseResponse_QNAME = new QName("http://upper/", "toUpperCaseResponse");
    private final static QName _ToUpperCase_QNAME = new QName("http://upper/", "toUpperCase");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ToUpperCaseResponse }
     * 
     */
    public ToUpperCaseResponse createToUpperCaseResponse() {
        return new ToUpperCaseResponse();
    }

    /**
     * Create an instance of {@link ToUpperCase }
     * 
     */
    public ToUpperCase createToUpperCase() {
        return new ToUpperCase();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ToUpperCaseResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://upper/", name = "toUpperCaseResponse")
    public JAXBElement<ToUpperCaseResponse> createToUpperCaseResponse(ToUpperCaseResponse value) {
        return new JAXBElement<ToUpperCaseResponse>(_ToUpperCaseResponse_QNAME, ToUpperCaseResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ToUpperCase }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://upper/", name = "toUpperCase")
    public JAXBElement<ToUpperCase> createToUpperCase(ToUpperCase value) {
        return new JAXBElement<ToUpperCase>(_ToUpperCase_QNAME, ToUpperCase.class, null, value);
    }

}
