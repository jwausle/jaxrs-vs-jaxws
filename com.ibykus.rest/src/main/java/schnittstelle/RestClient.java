package schnittstelle;

import com.google.common.io.CharStreams;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ContextResolver;

/**
 * Http rest client utility to GET,POST,PUT or DELETE a json,xml,text or raw resource with {@link ResteasyClient} under the hood.
 * <p/>
 * Use it like this
 * <pre>
 *  RestClient client = RestClient.builder()
 *    .host(this.host)
 *    .port(this.port)
 *    .registerProvider(new ObjectMapperJsonProvider());
 *
 *  // Get a server data
 *  Pojo body = client.httpRequest("/rest/pojo")
 *    .headerAcceptJson()
 *    .httpGet(Pojo.class));
 *
 *  // ... body.setAnyData()
 *
 *  // Update a server data
 *  Pojo updated = client.httpRequest("/rest/pojo/" + pojo.getId())
 *    .dataJson(body)
 *    .headerAcceptJson()
 *    .httpPost(Pojo.class));
 *
 *  // body.equals(updated) == true (SHOULD if no magic on server :) )
 * </pre>
 */
public class RestClient implements Closeable {
    private static final Entity<Object> EMPTY_TEXT_BODY = Entity.text("");
    private static final MediaType DEFAULT_CONTENT_TYPE = MediaType.TEXT_PLAIN_TYPE;
    private static final String HTTP = "http";
    private static final int HTTP_PORT = 80;
    private static final String HTTPS = "https";
    private static final Integer HTTPS_PORT = 443;

    /**
     * RestClient instance provider.
     *
     * @return not null builder.
     */
    public static RestClientBuilder builder() {
        return new RestClientBuilder();
    }

    private final String host;
    private final Optional<Integer> port;
    private final ResteasyClient resteasyClient;

    private RestClient(ResteasyClient resteasyClient, String host, Integer port) {
        this.resteasyClient = Objects.requireNonNull(resteasyClient, "resteasyClient");
        this.host = Objects.requireNonNull(host, "host");
        this.port = Optional.ofNullable(port);
    }

    /**
     * Create a httpRequest(Builder for 'https://host:port/') ready for GET,POST,PUT,DELETE.
     *
     * @return not null {@link Request}
     */
    public Request httpsRequest() {
        return new Request(this.resteasyClient, this.host, this.port.orElse(HTTPS_PORT)).https();
    }

    /**
     * Create a httpRequest(Builder for 'https://host:port/path') ready for GET,POST,PUT,DELETE.
     *
     * @param path not null url path
     * @return not null {@link Request}
     */
    public Request httpsRequest(String path) {
        Objects.requireNonNull(path, "path");
        return httpsRequest().addPath(path);
    }

    /**
     * Create a httpRequest(Builder for 'http://host:port/') ready for GET,POST,PUT,DELETE.
     *
     * @return not null {@link Request}
     */
    public Request httpRequest() {
        return new Request(this.resteasyClient, this.host, this.port.orElse(HTTP_PORT));
    }

    /**
     * Create a httpRequest(Builder for 'http://host:port/path') ready for GET,POST,PUT,DELETE.
     *
     * @param path not null url path
     * @return not null {@link Request}
     */
    public Request httpRequest(String path) {
        Objects.requireNonNull(path, "path");
        return httpRequest().addPath(path);
    }

    @Override
    public void close() throws IOException {
        if (this.resteasyClient != null) {
            this.resteasyClient.close();
        }
    }

    /**
     * HTTP Request builder to hide some request creation complexity.
     */
    public static class Request {
        private final Client httpClient;
        private final String host;
        private final Integer port;
        private final List<MediaType> acceptTypes = new LinkedList<>();
        private final AtomicReference<MediaType> contentType = new AtomicReference(MediaType.TEXT_PLAIN);
        private final AtomicReference<String> schema = new AtomicReference(HTTP);
        private Path path = Path.of("");
        private java.util.Optional<Object> data = java.util.Optional.empty();

        private Request(Client httpClient, String host, Integer port) {
            this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
            this.host = Objects.requireNonNull(host, "host");
            this.port = Objects.requireNonNull(port, "port");
        }

        public Request https() {
            this.schema.set(HTTPS);
            return this;
        }

        public Request headerAccept(MediaType mediaType) {
            java.util.Optional.ofNullable(mediaType).ifPresent(acceptTypes::add);
            return this;
        }

        public Request headerAcceptJson() {
            return headerAccept(MediaType.APPLICATION_JSON_TYPE);
        }

        public Request headerAcceptXml() {
            return headerAccept(MediaType.APPLICATION_XML_TYPE);
        }

        public Request headerAcceptText() {
            return headerAccept(MediaType.TEXT_PLAIN_TYPE);
        }

        public Request headerContentType(MediaType mediaType) {
            java.util.Optional.ofNullable(mediaType).ifPresent(contentType::set);
            return this;
        }

        /**
         * Set/overwrite the hole path if it not null.
         *
         * @param path ignore null paths
         * @return this
         */
        public Request path(Path path) {
            if (path != null) {
                this.path = path;
            }
            return this;
        }

        /**
         * Set/overwrite the hole path if it not null.
         *
         * @param path ignore null paths
         * @return this
         */
        public Request path(String path) {
            if (path != null) {
                this.path = Paths.get(path);
            }
            return this;
        }

        /**
         * Add the sub path if it not null.
         *
         * @param subpath ignore null paths
         * @return this
         */
        public Request addPath(Path subpath) {
            if (path != null) {
                this.path = this.path.resolve(subpath);
            }
            return this;
        }

        /**
         * Add the sub path if it not null.
         *
         * @param subpath ignore null paths
         * @return this
         */
        public Request addPath(String subpath) {
            if (path != null) {
                this.path = this.path.resolve(subpath);
            }
            return this;
        }

        /**
         * Set/overwrite the data body of the request with an instance. Use the default ContentType as header.
         *
         * @param data maybe null data
         * @return this
         */
        public Request data(Object data) {
            return data(data, DEFAULT_CONTENT_TYPE);
        }

        /**
         * Set/overwrite the data body of the request with an instance. Use the given contentType as header if not null.
         *
         * @param data        maybe null data
         * @param contentType ignore null types
         * @return this
         */
        public Request data(Object data, MediaType contentType) {
            this.headerContentType(contentType);
            this.data = java.util.Optional.ofNullable(data);
            return this;
        }

        /**
         * Set/overwrite the data body of the request with an instance. Use the 'application/json' as ContentType header.
         *
         * @param data maybe null data
         * @return this
         */
        public Request dataJson(Object data) {
            this.headerContentType(MediaType.APPLICATION_JSON_TYPE);
            this.data = java.util.Optional.ofNullable(data);
            return this;
        }

        /**
         * Set/overwrite the data body of the request with an instance. Use the 'application/xml' as ContentType header.
         *
         * @param data maybe null data
         * @return this
         */

        public Request dataXml(String data) {
            this.headerContentType(MediaType.APPLICATION_XML_TYPE);
            this.data = java.util.Optional.ofNullable(data);
            return this;
        }

        /**
         * Set/overwrite the data body of the request with an instance. Use the 'text/plain' as ContentType header.
         *
         * @param data maybe null data
         * @return this
         */
        public Request dataText(String data) {
            this.headerContentType(MediaType.TEXT_PLAIN_TYPE);
            this.data = Optional.ofNullable(data);
            return this;
        }

        /**
         * Do http(s) POST request.
         *
         * @param expectedResponseClass not null expected mappable Json|Xml class
         * @param <T>                   generic response body type (e.g. mappable Json|Xml class)
         * @return mapped response type or throw client or server exception.
         */
        public <T> T httpPost(Class<T> expectedResponseClass) {
            Objects.requireNonNull(expectedResponseClass, "expectedResponseClass");

            final URI uri = makeUri();
            final Invocation.Builder webTarget = this.httpClient.target(uri).request();
            this.acceptTypes.forEach(webTarget::accept);

            final Entity<Object> requestBody = data.map(localData -> Entity.entity(localData, this.contentType.get()))
                    .orElse(EMPTY_TEXT_BODY);

            final T responseBody = webTarget.post(requestBody, expectedResponseClass);
            return responseBody;
        }

        /**
         * Do http(s) GET request.
         *
         * @param expectedResponseClass not null expected mappable Json|Xml class
         * @param <T>                   generic response body type (e.g. mappable Json|Xml class)
         * @return mapped response type or throw client or server exception.
         */
        public <T> T httpGet(Class<T> expectedResponseClass) {
            Objects.requireNonNull(expectedResponseClass, "expectedResponseClass");
            final URI uri = makeUri();
            final Invocation.Builder client = this.httpClient
                    .target(uri).request();
            this.acceptTypes.forEach(client::accept);

            final T responseBody = client.get(expectedResponseClass);
            return responseBody;
        }

        /**
         * Do http(s) PUT request.
         *
         * @param expectedResponseClass not null expected mappable Json|Xml class
         * @param <T>                   generic response body type (e.g. mappable Json|Xml class)
         * @return mapped response type or throw client or server exception.
         */
        public <T> T httpPut(Class<T> expectedResponseClass) {
            Objects.requireNonNull(expectedResponseClass, "expectedResponseClass");
            final URI uri = makeUri();
            final Invocation.Builder webTarget = this.httpClient.target(uri).request();
            this.acceptTypes.forEach(webTarget::accept);

            final Entity<Object> requestBody = data.map(localData -> Entity.entity(localData, this.contentType.get()))
                    .orElse(EMPTY_TEXT_BODY);

            final T responseBody = webTarget.put(requestBody, expectedResponseClass);
            return responseBody;
        }

        /**
         * Do http(s) DELETE request.
         *
         * @param expectedResponseClass not null expected mappable Json|Xml class
         * @param <T>                   generic response body type (e.g. mappable Json|Xml class)
         * @return mapped response type or throw client or server exception.
         */
        public <T> T httpDelete(Class<T> expectedResponseClass) {
            Objects.requireNonNull(expectedResponseClass, "expectedResponseClass");
            final URI uri = makeUri();
            final Invocation.Builder client = this.httpClient
                    .target(uri).request();
            this.acceptTypes.forEach(client::accept);

            final T responseBody = client.delete(expectedResponseClass);
            return responseBody;
        }

        /**
         * Do http(s) GET request.
         *
         * @return raw response body as String or throw client or server exception.
         */
        public String httpGetRaw() {
            if (!acceptTypes.contains(MediaType.TEXT_PLAIN_TYPE)) {
                headerAccept(MediaType.TEXT_PLAIN_TYPE);
            }
            final Response response = httpGet(Response.class);
            final FilterInputStream bodyStream = (FilterInputStream) response.getEntity();
            String body = toString(bodyStream);
            return body;
        }

        private URI makeUri() {
            final UriBuilder builder = UriBuilder.fromPath("")
                    .scheme(this.schema.get())
                    .host(this.host)
                    .path(this.path.toString());

            Optional.ofNullable(this.port).ifPresent(builder::port);

            return builder
                    .build();
        }

        private String toString(FilterInputStream bodyStream) {

            String text = null;
            try (final Reader reader = new InputStreamReader(bodyStream)) {
                text = CharStreams.toString(reader);
            } catch (IOException e) {
                throw new RuntimeException("Cannot extract Response.body.", e);
            }
            return text;
        }

        public String toString() {
            return String.format("%s://%s:%s/%s -H 'ContentType: %s' -H 'Accept: %s' --data %s", this.schema, this.host, this.port, this.path, this.contentType, this.acceptTypes, this.data);
        }

    }

    /**
     * Wraps and enhance the {@link ResteasyClientBuilder} a little bit.
     */
    public static class RestClientBuilder extends ResteasyClientBuilder {
        private RestClientBuilder() {
            super();
        }

        private final AtomicReference<String> host = new AtomicReference<>("localhost");
        private final AtomicReference<Integer> port = new AtomicReference<>();

        public RestClientBuilder host(String host) {
            this.host.set(host);
            return this;
        }

        public RestClientBuilder port(Integer port) {
            this.port.set(port);
            return this;
        }

        public RestClientBuilder registerProvider(ContextResolver<?> provider) {
            super.register(provider);
            return this;
        }

        public RestClientBuilder registerAll(List<Object> provider) {
            provider.forEach(this::register);
            return this;
        }

        public RestClient buildRestClient() {
            final ResteasyClient resteasyClient = this.build();
            return new RestClient(resteasyClient, this.host.get(), this.port.get());
        }
    }
}
