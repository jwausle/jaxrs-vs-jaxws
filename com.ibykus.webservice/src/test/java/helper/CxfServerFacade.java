package helper;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.jws.WebService;
import javax.xml.namespace.QName;

/**
 * Test helper to start web server to host testable webservices. Use it like this:
 * <pre>
 * CxfServerFacade cxfFacade = CxfServerFacade
 *   .builder()
 *   .wsInterface(WebserviceInterface.class)
 *   .wsInstance(webserviceInstance)
 *   .localhostWithRandomPort()
 *   .build();
 * </pre>
 */
public class CxfServerFacade {
    public static CxfServerFacadeBuilder builder() {
        return new CxfServerFacadeBuilder();
    }

    private final AtomicReference<Server> server = new AtomicReference<>();
    private final Class<?> endpointClass;
    private final Object endpoint;
    private final QName serviceQName;
    private final String url;

    /**
     * Use {@link CxfServerFacadeBuilder}.{@code builder().build()} for construction.
     */
    private CxfServerFacade(Class<?> endpointInterface, Object endpoint, QName serviceQName, String url) {
        this.endpointClass = Objects.requireNonNull(endpointInterface, "endpointInterface");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.serviceQName = Objects.requireNonNull(serviceQName, "serviceQName");
        this.url = Objects.requireNonNull(url, "url");
    }

    public void startServer() {
        JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();

        svrFactory.setServiceClass(endpointClass);
        svrFactory.setAddress(this.url);
        svrFactory.setServiceBean(endpoint);
        svrFactory.setServiceName(serviceQName);

        final Server server = svrFactory.create();
        this.server.set(server);
    }

    public synchronized void teardownServer() {
        Optional.ofNullable(server.get()).ifPresent(Server::destroy);
    }

    public String getUrl() {
        return url;
    }

    public static class CxfServerFacadeBuilder {
        private Optional<Class<?>> endpointClass = Optional.empty();
        private Optional<Object> endpoint = Optional.empty();
        private Optional<String> serviceNamespace = Optional.empty();
        private Optional<String> serviceName = Optional.empty();
        private Optional<QName> serviceQName = Optional.empty();
        private Optional<String> host = Optional.empty();
        private Optional<Integer> port = Optional.empty();//
        private Optional<String> wsPath = Optional.empty();
        private Optional<String> url = Optional.empty();

        private CxfServerFacadeBuilder() {
        }

        public CxfServerFacadeBuilder wsInstance(Object wsInstance) {
            if (wsInstance != null) {
                this.endpoint = Optional.of(wsInstance);
            }
            return this;
        }

        public CxfServerFacadeBuilder wsInterface(Class<?> wsInterface) {
            if (wsInterface != null) {
                this.endpointClass = Optional.of(wsInterface);
            }
            return this;
        }

        public CxfServerFacadeBuilder inferWsInterface() {
            this.endpointClass = Optional.of(tryToInferWebserviceInterface(this.endpoint.get()));
            return this;
        }

        public CxfServerFacadeBuilder wsServiceName(String wsServiceName) {
            if (wsServiceName != null) {
                this.serviceName = Optional.of(wsServiceName);
            }
            return this;
        }

        public CxfServerFacadeBuilder wsServiceQName(QName wsServiceQName) {
            if (wsServiceQName != null) {
                this.serviceQName = Optional.of(wsServiceQName);
            }
            return this;
        }

        public CxfServerFacadeBuilder wsNamespace(String wsNamespace) {
            if (wsNamespace != null) {
                this.serviceNamespace = Optional.of(wsNamespace);
            }
            return this;
        }

        public CxfServerFacadeBuilder url(String url) {
            if (url != null) {
                this.url = Optional.of(url);
            }
            return this;
        }

        public CxfServerFacadeBuilder host(String host) {
            if (host != null) {
                this.host = Optional.of(host);
            }
            return this;
        }

        public CxfServerFacadeBuilder hostWithRandomPort(String host) {
            if (host != null) {
                this.host = Optional.of(host);
            }
            randomPort();
            return this;
        }

        public CxfServerFacadeBuilder path(String path) {
            if (path != null) {
                this.wsPath = Optional.of(path);
            }
            return this;
        }

        public CxfServerFacadeBuilder port(Integer port) {
            if (port != null) {
                this.port = Optional.of(port);
            }
            return this;
        }

        public CxfServerFacadeBuilder randomPort() {
            this.port = Optional.of(findFreePort());
            return this;
        }

        public CxfServerFacadeBuilder localhost() {
            host("localhost");
            return this;
        }

        public CxfServerFacadeBuilder localhostWithRandomPort() {
            localhost();
            randomPort();
            return this;
        }

        public CxfServerFacade build() {
            final Object endpointInstance = this.endpoint.orElseThrow(() -> new NullPointerException("endpoint"));
            final Class<?> endpointInterface = endpointClass.orElse(tryToInferWebserviceInterface(endpointInstance));

            // calculate serviceQName
            final String serviceName = this.serviceName.orElse(String.format("%sService", endpointInterface.getSimpleName()));
            final String serviceNamespace = this.serviceNamespace.orElse(reversePackages(endpointInterface));
            final QName serviceQName = this.serviceQName.orElse(new QName(serviceNamespace, serviceName));

            // calculate url
            final String endpointUrl = this.url.or(() -> {
                final Integer port = this.port.orElse(findFreePort());
                final String wsPath = this.wsPath.map(path -> path.startsWith("/") ? path : "/" + path).orElse("/ws");
                final String defaultLocalhost = String.format("http://localhost:%s%s", port, wsPath);
                final String url = this.host.map(host -> String.format("http://%s:%s%s", host, port, wsPath))
                        .orElse(defaultLocalhost);
                return Optional.of(url);
            }).get();

            final CxfServerFacade cxfFacade = new CxfServerFacade(endpointInterface, endpointInstance, serviceQName, endpointUrl);
            return cxfFacade;
        }

    }

    private static Class<?> tryToInferWebserviceInterface(final Object wsInstance) {
        final Class<?> wsClassAsDefault = Objects.requireNonNull(wsInstance).getClass();
        final WebService wsAnnotationOrNull = wsClassAsDefault.getAnnotation(WebService.class);

        final Class<?> inferredOrDefaultClass = Optional.ofNullable(wsAnnotationOrNull)
                .map(WebService::endpointInterface)
                .flatMap(CxfServerFacade::safeClassForName)
                .or(() -> {
                    Optional<Object> wsInterface = Optional.empty();

                    final Class<?>[] interfaces = wsClassAsDefault.getInterfaces();
                    for (Class<?> _interface : interfaces) {
                        if (_interface.getAnnotation(WebService.class) != null) {
                            wsInterface = Optional.of(_interface);
                            break;
                        }
                    }

                    return wsInterface.map(Object::getClass);
                }).orElse(wsClassAsDefault);

        return inferredOrDefaultClass;
    }

    private static Optional<Class<?>> safeClassForName(String interfaceClass) {
        try {
            return Optional.of(Class.forName(interfaceClass));
        } catch (ClassNotFoundException e) {
            System.out.println("Ignore " + e.getMessage() + " for testing");
            return Optional.empty();
        }
    }

    /**
     * Finds a free localhost port.
     *
     * @return port number.
     */
    private static int findFreePort() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(0);
            int port = server.getLocalPort();
            server.close();
            return port;
        } catch (IOException e) {
            throw new RuntimeException("No free network port");
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
}
