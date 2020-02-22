# JAX-RS Rest Schnittstelle

JAX-RS alias [JSR-339](https://jcp.org/en/jsr/detail?id=339) (EE7),[JSR-370](https://jcp.org/en/jsr/detail?id=370) (EE8) bezeichnet und spezifiziert die Restschnittstelle von Java EE.
Im wildfly Universum wird JAX-RS von Resteasy bereitgestellt. Im wildfly15(EE8) wird z.B. die JAX-RS Version 2.1(JSR-370) 
vom Modul Resteasy-3.2.6 implementiert und bereitgestellt.

* [API](https://docs.oracle.com/javaee/7/api/javax/ws/rs/package-tree.html) - JAX-RS 2.1/JSR-370 ([mvn](https://mvnrepository.com/artifact/org.jboss.spec.javax.ws.rs/jboss-jaxrs-api_2.1_spec/2.0.1.Final)) 
* Impl - [Resteasy](https://resteasy.github.io/) 3.6.2.Final ([mvn](https://mvnrepository.com/artifact/org.jboss.resteasy/resteasy-jaxrs/3.6.2.Final)) 

[Rest](https://docs.microsoft.com/de-de/azure/architecture/best-practices/api-design) bezeichnet dabei den statuslosen 
Zugriff auf Webresourcen basierend auf [Http](https://tools.ietf.org/html/rfc2616) POST,PUT,GET,DELETE. 
Die Datenobject werden i.d.R im JSON Format ausgetauscht. 

## JAX-RS Server Endpoint 

```java
@Path("/pojo")
public class RestEndpoint {
  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public PojoList httpGet() { ... }

  @GET
  @Path("{id}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Pojo httpGet(@PathParam("id") String id) { ... }

  @POST
  @Path("{id}")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Pojo httpPost(@PathParam("id") String idOrNull, Pojo requestBody) { ... }

  @PUT
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Pojo httpPut(Pojo requestBody) { ... }

  @POST
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Pojo httpPost(Pojo requestBody) { ... }

  @DELETE
  @Path("{id}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Pojo httpDelete(@PathParam("id") String id) { ... }
}

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Pojo
{
  static final String JSON_ID = "id";
  static final String JSON_VALUE = "value";

  @JsonCreator
  public Pojo(@JsonProperty(JSON_ID) String id, @JsonProperty(JSON_VALUE) int value) { ... }

  @JsonGetter(JSON_ID)
  @XmlElement(name=JSON_ID)
  public String getId() { ... }

  @JsonGetter(JSON_VALUE)
  @XmlElement(name=JSON_VALUE)
  public Integer getValue() { ... }
} 

@ApplicationPath("/rest") // wird in web.xml überschrieben
public class RestApplication extends Application
{
  @Override // Optional - it's more clear than trust for runtime annotations
  public Set<Class<?>> getClasses() { return Collections.singleton(RestEndpoint.class); }

  @Override // Optional - it's more clear than trust for runtime annotations
  public Set<Object> getSingletons() { ... } // JsonMapper, XmlMapper
}

# ibykus.war!/WEB-INF/web.xml
<servlet-mapping>
   <servlet-name>RestApplication</servlet-name>
   <url-pattern>/overwrite-subpath/*</url-pattern>
</servlet-mapping>
```

### Mixin von Json(JAXP) und Xml(JAXB) Annotationen 

Das Mixin von Json(JAXP) und Xml(JAXB) Annotationen wird nicht wie erwarted unterstützt. Das betrifft die `@XmlRootElement` Annotation einer Pojo
Klasse. Die Serialisierung einer einzelnen Json/Xml `Pojo` Instanz klappt wie erwartet. Aber das Serialisieren
einer Liste liefert ein kurioses JsonArray.

* erwarted - `[{pojo.json},...,{pojo.json}]`
* tatsächlich - `[pojo:{pojo.json},...,pojo:{pojo.json}]` 

> Jedes Json `Pojo` in der Liste bekommt einen Key, der vom Klassennamen abgeleitet wird. Das repräsentiert, bei der Xml
Serialisierung das XmlElement `<pojo>`. 

Aus diesem Grund, wird die `@XmlRootElement` an der `Pojo` Klasse nicht empfohlen. Sondern für die Listendarstellung sollte eine
eigene `PojoList` Klasse eingeführt werden, die diese Besonderheit egalisert.

```java
@XmlRootElement(name= PojoList.POJOS)
public class PojoList
{
  static final String POJOS = "pojos";
  static final String POJO = "pojo";

  private final List<Pojo> pojos = new ArrayList<>();

  PojoList(){/* MUST for @XmlRootElement */}

  @JsonCreator
  public PojoList(@JsonProperty(POJO) Collection<Pojo> pojos){
    if(pojos != null){ this.pojos.addAll(pojos); }
  }

  @XmlElement(name=POJO)
  @JsonGetter(POJOS)
  public List<Pojo> getPojos() { return this.pojos; }
}
```

Dieser Pojo ListWrapper (de)serialisiert folgendes: 

* Json - `[{pojo.json},...{pojo.json}]`
* Xml - `<pojos><pojo>..</pojo>...<pojo>..</pojo></pojos>`

## JAX-RS Client

Der `RestClient` ist eine Helperklasse, um das Bauen und Ausführen eines HTTP Rest Requests möglichst einfach zu gestallten.

```java
public class RestClient
{
  public static RestClientBuilder builder()  { ... }

  private RestClient(ResteasyClient resteasyClient, String host, Integer port)
  {
    this.resteasyClient = Objects.requireNonNull(resteasyClient, "resteasyClient");
    this.host = Objects.requireNonNull(host, "host");
    this.port = Optional.ofNullable(port);
  }

  public Request httpsRequest() { ... }

  public Request httpsRequest(String path) { ... }

  public Request httpRequest() { ... }

  public Request httpRequest(String path) { ... }
  
  public final class Request { 
    public <T> T httpGet() { ... }
    public <T> T httpPut(Class<T> responseType) { ... }
    public <T> T httpPost(Class<T> responseType) { ... }
    public <T> T httpDelete(Class<T> responseType) { ... }
  }
  
  public final class RestClientBuilder { ... }
}   
```

## Unit testing

Für pure Unittests gibt es keine Einschränkungen oder Hinweise, die man beachten muss. 

## Integration testing

Ein Integrationstest hat einen tatsächliche Request-Response Test über ein Netzwerkinterface (z.B. localhost) zum Ziel. 
Dazu muss es einfach und schnell möglich sein einen Http Server Thread, mit allen EE Reserouce (Applications,Endpoints,...) 
zu starten und zu stoppen. Ein mögliches Hilfsmittel könnte eine `ResteasyServerFacade` sein, die genau das ermöglicht.
  
```java
// Beispiel Integrationtest
public class IntegrationTest {
    private ResteasyServerFacade resteasyFacade;
    
    @Before
    public void beforeTest()
    {
      final ResteasyServerFacade newResteasy = ResteasyServerFacade.builder()              .configureApplication(new RestApplication())
              .configureProvider(new ObjectMapperJsonProvider())
              .configureResources(new RestEndpoint())
              .build();
      this.resteasyFacade = Objects.requireNonNull(newResteasy, "newResteasy");
      this.resteasyFacade.startServer();
    }
  
    @After
    public void afterTest() {
      resteasyFacade.teardownServer();
    }
    
    @Test
    public void test() { 
      PojoList response = resteasyFacade.request("/pojo").httpGet();
      ...
    }
}

// Use ResteasyFacade to start and stop http server on localhost.
public class ResteasyServerFacade
{
  public static final ResteasyServerBuilder builder() { ... }

  public void startServer() { ... }
  public void teardownServer() { ... }
  
  public RestClient.Request request() { ... }
  public RestClient.Request request(String path) { ... }
  
  public static class ResteasyServerBuilder 
  {
    public ResteasyBuilder configureApplication(RestApplication restApplication) { ... } // add ee.Application
    public ResteasyBuilder configureProvider(Object provider) { ... } // add JsonMapper,XmlMapper
    public ResteasyBuilder configureResources(Object endpoint) { ... } // add endpoints
    public ResteasyBuilder port(Integer port) { ... } // or use a free random one
    public ResteasyBuilder host(String host) { ... } // or use localhost
    public ResteasyFacade build() { ... }
  }
 }
```

## Maven Specials

Die meisten JAX-RS spezifischen Abhängigkeiten sind in diesem Artifact BOM enthalten.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.wildfly.bom</groupId>
            <artifactId>wildfly-javaee8</artifactId>
            <version>${wildfly-javaee8.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
Durch die Definition werden alle im `wildfly-javaee8:bom` enthaltenden managed dependencies in diesen `<dependencyManagement>`
Block `importiert`. Das bedeutet, das ich mir über eine konkrete Artifaktversion keine Gedanken machen
muss. Um ein Artifakt zu nutzen, muss ich es noch als `<dependency>` erklären. Einige wichtige Artifakte
werden im Folgenden genannt. In dieser JAX-RS Spezifikation befinden sich alle JAX-RS spezifischen [Interfaces und Annotationen](https://docs.oracle.com/javaee/7/api/javax/ws/rs/package-tree.html).

```xml
<dependency>
    <groupId>org.jboss.spec.javax.ws.rs</groupId>
    <artifactId>jboss-jaxrs-api_2.1_spec</artifactId>
    <scope>provided</scope>
</dependency>
```

Diese Spezifikation wird von Resteasy in folgenden Artifakt implementiert.

````xml
<dependency>
    <groupId>org.jboss.resteasy</groupId>
    <artifactId>resteasy-jaxrs</artifactId>
    <scope>provided</scope>
</dependency>
````

Die entsprechenden Resteasy Client Klassen, um Rest Reseourcen bei einem Http Server anzufragen, findet man in dem nächsten Artifakt.

```xml
<dependency>
    <groupId>org.jboss.resteasy</groupId>
    <artifactId>resteasy-client</artifactId>
    <scope>provided</scope>
</dependency>
``` 

Um einen Http Server zu Testzwecken, inkl. einer deployten Rest Anwendung, zu starten sollte man dieses Artifact benutzen. 

```xml
<dependency>
    <groupId>org.jboss.resteasy</groupId>
    <artifactId>resteasy-undertow</artifactId>
    <scope>test</scope>
</dependency>
```

Um die Xml (De)Serialisierung im Mixin zu unterstützen, muss das folgende Artifakt benutzt werden. 
 
```xml
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-xml</artifactId>
    <version>${jackson-dataformat-xml.version}</version>
    <scope>compile</scope>
</dependency>
```

