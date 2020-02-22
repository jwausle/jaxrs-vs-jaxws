package schnittstelle;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/**
 * Helper class to get access to a remote {@link javax.jws.WebService} interface instance. Use the {@link WebserviceClientBuilder} to create an instance of it.
 */
class WebserviceClient {
    public static WebserviceClientBuilder builder() {
        return new WebserviceClientBuilder();
    }

    private final URL endpointUrl;
    private final QName serviceNamespace;

    private WebserviceClient(URL serviceEndpoint, QName qName) {
        this.endpointUrl = Objects.requireNonNull(serviceEndpoint, "endpointUrl");
        this.serviceNamespace = Objects.requireNonNull(qName, "qName");
    }

    public <T> T proxy(Class<T> webserviceInterface) {
        URL wsdlUrl = endpointUrl;
        QName serviceName = serviceNamespace;

        final Service service = Service.create(ensureWsdlSuffix(wsdlUrl), serviceName);
        return service.getPort(webserviceInterface);
    }

    /**
     * The builder hide the complexity of namespace and url building depending on webservice interface class name (package+class).
     */
    public static class WebserviceClientBuilder {
        private URL serviceEndpoint;
        private Optional<QName> serviceQName = Optional.empty();

        private WebserviceClientBuilder() {
        }

        public WebserviceClientBuilder endpoint(String url) {
            Objects.requireNonNull(url, "url");
            try {
                return endpoint(new URL(url));
            } catch (MalformedURLException e) {
                throw new RuntimeException("NO url=" + url, e);
            }
        }

        public WebserviceClientBuilder endpoint(URL url) {
            this.serviceEndpoint = Objects.requireNonNull(url, "url");
            return this;
        }

        public WebserviceClientBuilder webservice(QName service) {
            this.serviceQName = Optional.of(Objects.requireNonNull(service, "service"));
            return this;
        }

        /**
         * Derive service name space from service class -> http://reverse.packages.from.serviceClass. And call webservice(QName).
         *
         * @param serviceClass not null service interface class
         * @return this builder
         */
        public WebserviceClientBuilder webservice(Class<?> serviceClass) {
            Objects.requireNonNull(serviceClass, "serviceClass");

            final String serviceNamespace = reversePackages(serviceClass);
            return webservice(serviceClass, serviceNamespace);
        }

        /**
         * Construct a QName(serviceNamespace, serviceClass.simpleName + Service) and call webservice(QName).
         *
         * @param serviceClass     not null service interface class
         * @param serviceNamespace not null service namespace (e.g. http://reverse.packages.from.serviceClass)
         * @return this builder
         */
        public WebserviceClientBuilder webservice(Class<?> serviceClass, String serviceNamespace) {
            Objects.requireNonNull(serviceClass, "serviceClass");
            Objects.requireNonNull(serviceNamespace, "serviceNamespace");

            final String serviceName = String.format("%sService", serviceClass.getSimpleName());
            final QName serviceQName = new QName(serviceNamespace, serviceName);
            return webservice(serviceQName);
        }

        public WebserviceClient build() {
            Objects.requireNonNull(this.serviceEndpoint, "Webservice endpoint url expected. Use builder.serviceEndpoint(url) to set one.");
            Objects.requireNonNull(this.serviceQName.orElse(null), "Webservice namespace expected. Use builder.webservice(qname) to set one.");

            return new WebserviceClient(this.serviceEndpoint, this.serviceQName.get());
        }
    }

    /**
     * Takes 'a.b.c.ServiceClass' and return 'c.b.a' as String.
     *
     * @param serviceClass a.b.c.ServiceName
     * @return c.b.a as String
     */
    private static String reversePackages(Class<?> serviceClass) {
        final Path serviceClassAsPath = Paths.get(serviceClass.getName().replace(".", "/"));

        final Path serviceClassName = serviceClassAsPath.getFileName();
        assert serviceClass.getSimpleName().equals(serviceClassName.toString()) : "className must be the file name of the path";

        final Path servicePackageAsPath = serviceClassAsPath.subpath(0, serviceClassAsPath.getNameCount() - 1);

        List<String> servicePackageAsList = new LinkedList<>();
        servicePackageAsPath.iterator().forEachRemaining(packageElement -> servicePackageAsList.add(packageElement.toString()));
        Collections.reverse(servicePackageAsList);

        return String.format("http:/%s/", servicePackageAsList.isEmpty() ? "unknown" : servicePackageAsList.stream().reduce("", (left, right) -> left + "/" + right));
    }

    /**
     * Ensure that given URL 'http://any.host/path' ends with '?wsdl'
     *
     * @param wsdlUrl not null URL (e.g. http://any.host/path)
     * @return 'http://any.host/path?wsld' if given URL not ends with 'http://any.host/path?wsdl' already
     */
    private static URL ensureWsdlSuffix(URL wsdlUrl) {
        URL urlWithWsdlSuffix;
        if (!"wsdl".equalsIgnoreCase(wsdlUrl.getQuery())) {
            try {
                urlWithWsdlSuffix = new URL(wsdlUrl.toString() + "?wsdl");
            } catch (MalformedURLException e) {
                throw new RuntimeException("Cannot append ?wsdl to url:" + wsdlUrl, e);
            }
        } else {
            urlWithWsdlSuffix = wsdlUrl;
        }
        return urlWithWsdlSuffix;
    }
}
