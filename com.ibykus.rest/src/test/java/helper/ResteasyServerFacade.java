package helper;

import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentInfo;
import schnittstelle.ObjectMapperJsonProvider;
import schnittstelle.RestApplication;
import schnittstelle.RestClient;

/**
 * Start a HTTP server on localhost:RANDOM_PORT with deployed EE8 {@link Application}. The server simulates an running wildfly instance as {@link
 * UndertowJaxrsServer}.
 */
public class ResteasyServerFacade {
    public static final ResteasyBuilder builder() {
        return new ResteasyBuilder();
    }

    private static final String DEFAULT_APPLICATION_PATH = "/";

    private final String host;
    private final Integer port;
    private final Optional<Application> application;
    private final List<Object> resources = new CopyOnWriteArrayList<>();
    private final List<Object> provider = new CopyOnWriteArrayList<>();

    private final AtomicReference<UndertowJaxrsServer> httpServer = new AtomicReference<>();
    private final AtomicReference<RestClient> httpClient = new AtomicReference<>();


    private ResteasyServerFacade(String host, Integer port, Optional<Application> application, List<Object> resources, List<Object> provider) {
        this.host = Objects.requireNonNull(host, "host");
        this.port = Objects.requireNonNull(port, "port");
        this.application = Objects.requireNonNull(application, "application");
        this.resources.addAll(Objects.requireNonNull(resources, "resources"));
        this.provider.addAll(Objects.requireNonNull(provider, "provider"));
    }

    /**
     * Start a 'http://host:port' server and deploy an in memory war for configured application. Call <code>teardownServer()</code> if already another server is
     * started.
     */
    public void startServer() {
        // Start HTTP server
        UndertowJaxrsServer server = new UndertowJaxrsServer();
        Undertow.Builder serverBuilder = Undertow.builder().addHttpListener(this.port, this.host);
        server.start(serverBuilder);

        // Configure a InMemory-WAR deployment
        ResteasyDeployment deployment = new ResteasyDeployment();
        this.application.ifPresent(deployment::setApplication);
        deployment.setResources(this.resources);
        deployment.setProviders(this.provider);

        DeploymentInfo inMemoryWar = server.undertowDeployment(deployment);
        inMemoryWar.setClassLoader(ResteasyServerFacade.class.getClassLoader());
        inMemoryWar.setDeploymentName(this.getClass().getName());
        inMemoryWar.setContextPath(determineContextPath(this.application));

        // Deploy InMemory-WAR
        server.deploy(inMemoryWar);

        this.httpServer.set(server);

        // Configure a HTTP client
        final RestClient.RestClientBuilder builder = RestClient.builder()
                .host(this.host)
                .port(this.port)
                .registerProvider(new ObjectMapperJsonProvider())
                .registerAll(this.provider);
        this.httpClient.set(builder.buildRestClient());
    }

    /**
     * Stop started 'http://host:port' server and close existing clients.
     */
    public void teardownServer() {
        {
            if (httpClient.get() != null) {
                try {
                    httpClient.get().close();
                } catch (IOException e) {
                    System.out.println("Test httpClient closing throws IOExeception: " + e.getMessage() + ". No panic - it happen after testing.");
                } finally {
                    httpClient.set(null);
                }
            }
        }
        if (httpServer.get() != null) {
            httpServer.get().stop();
            httpServer.set(null);
        }
    }

    /**
     * Create a httpRequest(Builder for 'http://host:port/') ready for GET,POST,PUT,DELETE.
     *
     * @return not null {@link RestClient.Request}
     */
    public RestClient.Request request() {
        return this.httpClient.get().httpRequest();
    }

    /**
     * Create a httpRequest(Builder for 'http://host:port/path') ready for GET,POST,PUT,DELETE.
     *
     * @param path not null url path
     * @return not null {@link RestClient.Request}
     */
    public RestClient.Request request(String path) {
        Objects.requireNonNull(path, "path");
        return request().addPath(path);
    }

    /**
     * {@link ResteasyServerFacade} builder to setup an instance for 'http://host:port' requests.
     */
    public static class ResteasyBuilder {
        private final AtomicReference<String> host = new AtomicReference<>("localhost");
        private final AtomicReference<Integer> port = new AtomicReference<>(0);
        private final AtomicReference<Application> application = new AtomicReference<>();
        private final List<Object> resources = new LinkedList<>();
        private final List<Object> provider = new LinkedList<>();

        public ResteasyBuilder configureApplication(RestApplication restApplication) {
            application.set(restApplication);
            return this;
        }

        public ResteasyBuilder configureProvider(Object provider) {
            Objects.requireNonNull(provider, "provider");
            this.provider.add(provider);
            return this;
        }

        public ResteasyBuilder configureResources(Object endpoint) {
            Objects.requireNonNull(endpoint, "endpoint");
            this.resources.add(endpoint);
            return this;
        }

        public ResteasyBuilder port(Integer port) {
            this.port.set(port);
            return this;
        }

        public ResteasyBuilder host(String host) {
            this.host.set(host);
            return this;
        }

        public ResteasyServerFacade build() {
            if (port.get() == 0) {
                port.set(findFreePort());
            }
            final ResteasyServerFacade resteasy = new ResteasyServerFacade(host.get(), port.get(), Optional.ofNullable(this.application.get()), resources, provider);
            return resteasy;
        }
    }

    /**
     * Finds a free httpServer port.
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

    private static String determineContextPath(Optional<Application> optionalApplication) {
        if (optionalApplication.isPresent()) {
            final Application testApplication = optionalApplication.get();
            if (testApplication == null || !testApplication.getClass().isAnnotationPresent(ApplicationPath.class)) {
                return DEFAULT_APPLICATION_PATH;
            }
            return testApplication.getClass().getAnnotation(ApplicationPath.class).value();
        }
        return DEFAULT_APPLICATION_PATH;
    }
}
