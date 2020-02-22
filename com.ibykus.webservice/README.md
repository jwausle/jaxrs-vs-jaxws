# JAX-WS Webservice Schnittstelle

JAX-WS alias [JSR-224](https://jcp.org/en/jsr/detail?id=224) bezeichnet und spezifiziert
die Werservice Schnittstelle seit Java EE seit Version 7.

* API - Version 2.2 `org.jboss.spec.javax.xml.ws:jboss-jaxws-api_2.2_spec:jar:2.0.2.Final`
* IMPL - [CXF](https://cxf.apache.org/) von Apache (im wildfly 15 cxf-3.2.5)

## JAX-WS Server Endpoint 

```java
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface WebserviceEndpoint {
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

@WebService(endpointInterface = "WebserviceEndpoint", serviceName = "WebserviceEndpoint")
public class WebserviceEndpointImpl implements WebserviceEndpoint {
    @Override
    public Pojo create() { }

    @Override
    public PojoList readAll() { }

    @Override
    public Pojo read(String id) { }

    @Override
    public Pojo update(Pojo pojoOrNull) { }

    @Override
    public Pojo delete(String id) { }
}

@XmlRootElement // that's different to JAX-RS Pojo.class.
public final class Pojo {
    @XmlElement(name = JSON_ID)
    public String getId() { }
    @XmlElement(name = JSON_VALUE)
    public Integer getValue() { }
}

@XmlRootElement(name = PojoList.POJOS)
public class PojoList {
    @XmlElement(name = POJO)
    public List<Pojo> getPojos() { }
}
```

## JAX-WS Client

```java
class WebserviceClient {
    public static WebserviceClientBuilder builder() { }

    public <T> T proxy(Class<T> webserviceInterface) { }

    public static class WebserviceClientBuilder {
        public WebserviceClientBuilder endpoint(URL url) { }
        public WebserviceClientBuilder webservice(QName service) { }
        public WebserviceClient build() { }
    }
}
```

## Unit testing

Für pure Unittests gibt es keine Einschränkungen oder Hinweise, die man beachten muss. 

## Integration testing

Ein Integrationstest hat einen tatsächliche Request-Response Test über ein Netzwerkinterface (z.B. localhost) zum Ziel. 
Dazu muss es einfach und schnell möglich sein einen Http Server Thread, mit allen EE Reserouce (Applications,Endpoints,...) 
zu starten und zu stoppen. Ein mögliches Hilfsmittel könnte eine `CxfServerFacade` sein, die genau das ermöglicht.

```java
public class CxfServerFacade {
    public static CxfServerFacadeBuilder builder() { }

    public void startServer() {
        JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();

        svrFactory.setServiceClass(endpointClass);
        svrFactory.setAddress(this.url);
        svrFactory.setServiceBean(endpoint);
        svrFactory.setServiceName(serviceQName);

        final Server server = svrFactory.create();
        this.server.set(server);
    }

    public synchronized void teardownServer() {}

    public static class CxfServerFacadeBuilder {
        public CxfServerFacadeBuilder wsInstance(Object wsInstance) {}
        public CxfServerFacadeBuilder wsInterface(Class<?> wsInterface) {}
        public CxfServerFacadeBuilder inferWsInterface() {}
        public CxfServerFacadeBuilder wsServiceName(String wsServiceName) {}
        public CxfServerFacadeBuilder wsServiceQName(QName wsServiceQName) {}
        public CxfServerFacadeBuilder wsNamespace(String wsNamespace) {}
        public CxfServerFacadeBuilder url(String url) {}
        public CxfServerFacadeBuilder host(String host) {}
        public CxfServerFacadeBuilder localhost() {}
        public CxfServerFacadeBuilder port(Integer port) {}
        public CxfServerFacadeBuilder randomPort() {}

        public CxfServerFacade build() { }
    }
}
```

Ein Integrationstest sieht dann folgendermassen aus.

```java
public class Integrationstest {
    private final WebserviceEndpointImpl testSubject = new WebserviceEndpointImpl();
    
    private final CxfServerFacade cxfFacade = CxfServerFacade.builder()
            .wsInstance(testSubject)
            .wsInterface(WebserviceEndpoint.class)
            .localhost()
            .randomPort()
            .build();

    @Before
    public void beforeTest() { cxfFacade.startServer();}

    @After
    public void afterTest() {  cxfFacade.teardownServer(); }

    @Test
    public void test() {
        final WebserviceEndpoint proxy = WebserviceClient.builder()
                .endpoint(cxfFacade.getUrl())
                .webservice(WebserviceEndpoint.class)
                .build()
                .proxy(WebserviceEndpoint.class);

        final Pojo pojo = proxy.create();
        Assert.assertNotNull(pojo);

        final Pojo read = proxy.read(pojo.getId());
        Assert.assertEquals(pojo, read);
    }
}
```

## Maven Specials

Alle jaxws Abhängikeiten werden im `wildfly-jaxws-client-bom:bom` erklärt. Bis zur `wildfly:17` ist das
allerdings kein echtes `BOM`. Denn alle Artifakte werden als `<dependency>` und nicht als `<dependencyManagement>`
erklärt. Somit hätte `<scope>import</scope>` nicht den erwarteten Effekt. 

Aus diesem Grund muss bis zur version `wildfly:17` das `wildfly-jaxws-client-bom:bom` als `<dependency>`
erklärt werden. Damit werden dann automatisch alle bom dependencies transient geerbt. 

> Das kann unerwünschte Seiteneffekte haben.

```xml
   <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.wildfly</groupId>
                <artifactId>wildfly-jaxws-client-bom</artifactId>
                <version>${wildfly-jaxws-client-bom.version}</version>
                <type>pom</type>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <dependencies>
        <dependency>
            <groupId>org.wildfly</groupId>
            <artifactId>wildfly-jaxws-client-bom</artifactId>
            <type>pom</type>
        </dependency>

        <!-- test dependencies -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>${assertj-core.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
```

