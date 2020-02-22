package schnittstelle;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;

/**
 * JAX-WS webservice interface.
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface WebserviceEndpoint {
    String WEBSERVICE_INTERFACE = "schnittstelle.WebserviceEndpoint";
    String WEBSERVICE_NAME = "WebserviceEndpoint";
    QName WEBSERVICE_QNAME = new QName("http://schnittstelle/", WEBSERVICE_NAME + "Service");

    @WebMethod
    Pojo create();

    @WebMethod
    PojoList readAll();

    @WebMethod
    Pojo read(String id);

    @WebMethod
    Pojo update(Pojo pojo);

    @WebMethod
    Pojo delete(String id);
}
