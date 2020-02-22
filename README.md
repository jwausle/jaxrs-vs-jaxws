# EE Schnittstellen JAX-RS vs JAX-WS

JAX-RS und JAX-WS sind 2 Java EE Webschnittstellen Spezifikationen

1. JAX-RS ist die [Http-REST-Schnittstelle](https://www.ics.uci.edu/~fielding/pubs/dissertation/top.htm) (Statuslose Resourcen Kommunikation) 
2. JAX-WS ist die etwas ältere Webservice-Schnittstelle (Mehr Staus mir mehr Mögichkeiten)

## JAX-RS Rest Schnittstelle

JAX-RS ist eine Sammlung von [Annotationen und Interfaces](..), die es ermöglichen [REST](https://www.ics.uci.edu/~fielding/pubs/dissertation/top.htm) 
im Wildfly EE Container zu ermöglichen. REST ist dabei

* ein Remote Aufruf einer Recource über HTTP
* der Remote Aufruf liefert JSON/XML Elemente oder Element-Listen als Ergebnis
* dieses Ergebniss ist [idempotent](https://de.wikipedia.org/wiki/Idempotenz) ~ Statuslos ~ gleicher Aufruf liefert das gleiche Ergebnis

REST im EE/Wildfly Kontext heisst:

* Annotiere HttpEndpoints mit `@Path` und `@Consume(ContentType),@Produce(Accept)`
* Annotiere Pojos mit `@Json..` und `@Xml..`

Und vertraue auf die Maggie des Wildfly, dass alles funktioniert wie gewünscht. Im Grunde ist das auch
so. Ein Entwickler, der nicht jeden Tag EE-REST Endpunkte schreibt, benötigt allerdings ab und an ein paar 
mehr Infos. Die findest du [hier](./com.ibykus.rest/README.md). 

## JAX-WS Webservice Schnittstelle

JAX-WS ist eine Sammlung von [Annotation und Interfaces](..), die es ermöglichen Java-Interfaces Remote aufzurufen. 

* Die Serverendpunkte werden mit `@Webserice` und `@WebMethode` annotiert
* Die Daten werden mit `@Xml..` annotiert
* Anhand der Annotationen wird ein WSDL Schnittstellen Dokument abgeleitet/generiert. Dies kann zur Entwicklungszeit oder zur Laufzeit passieren
* Anhand dieses WSDL Dokuments kann ein Potentieller Client ein Proxy-Objekt zur Laufzeit instanzieren
* Mit dieser Proxy-Instanz kann der Client das Anfragen an den Webserivce Endpunkt stellen
* Das Übertragungsprotokoll ist dabei `SOAP` oder `HTTP`   
 
Im Gegensatz zu REST, ist der Webservice Ansatz nicht zwingend Statuslos. Die Aufruf- und 
Serialisierungs-Magie wird zur Laufzeit ebenfalls vom EE/Wildfly Container übernommen. Zur Entwicklungszeit
wünscht man allerdings doch ein paar Möglichkeiten mehr. Auf die wird [hier](./com.ibykus.webservice/README.md) eingegangen.

## Maven 

Alle JAX-RS und JAX-WS Abhängigkeiten für eine bestimmte Wildfly Version sind in dem folgenden BOM enthalten.

* https://mvnrepository.com/artifact/org.wildfly.bom/wildfly-javaee8

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.wildfly.bom</groupId>
            <artifactId>wildfly-javaee8</artifactId>
            <version>${wildfly.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Für `wildfly.version=15.0.1.Final` sind das die Folgenden.

```
org.wildfly:wildfly-ejb-client-bom:15.0.1.Final
org.wildfly:wildfly-jaxws-client-bom:15.0.1.Final
org.wildfly:wildfly-jms-client-bom:15.0.1.Final
org.hibernate:hibernate-core:5.3.7.Final
org.hibernate:hibernate-entitymanager:5.3.7.Final
org.hibernate:hibernate-envers:5.3.7.Final
org.hibernate:hibernate-jpamodelgen:5.3.7.Final
org.hibernate:hibernate-search-engine:5.10.3.Final
org.hibernate:hibernate-search-orm:5.10.3.Final
org.hibernate.validator:hibernate-validator:6.0.14.Final
org.hibernate.validator:hibernate-validator-annotation-processor:6.0.14.Final
org.infinispan:infinispan-core:9.4.3.Final
org.infinispan:infinispan-client-hotrod:9.4.3.Final
org.infinispan:infinispan-directory-provider:9.4.3.Final
org.jboss.ejb3:jboss-ejb3-ext-api:2.2.0.Final
org.jboss.logging:jboss-logging-processor:2.1.0.Final
org.jboss.logging:jboss-logging-annotations:2.1.0.Final
org.jboss.logging:jboss-logging:3.3.2.Final
org.jboss.narayana.xts:jbossxts:5.9.0.Final
org.jboss.resteasy:resteasy-atom-provider:3.6.2.Final
org.jboss.resteasy:resteasy-jaxb-provider:3.6.2.Final
org.jboss.resteasy:resteasy-jettison-provider:3.6.2.Final
org.jboss.resteasy:resteasy-jackson-provider:3.6.2.Final
org.jboss.resteasy:resteasy-jackson2-provider:3.6.2.Final
org.jboss.resteasy:resteasy-jaxrs:3.6.2.Final
org.jboss.resteasy:resteasy-client:3.6.2.Final
org.jboss.resteasy:resteasy-multipart-provider:3.6.2.Final
org.jboss.resteasy:resteasy-json-p-provider:3.6.2.Final
org.jboss.resteasy:resteasy-jsapi:3.6.2.Final
org.jboss.resteasy:resteasy-validator-provider-11:3.6.2.Final
org.jboss.resteasy:resteasy-spring:3.6.2.Final
org.jboss.security:jboss-negotiation-common:3.0.4.Final
org.jboss.security:jboss-negotiation-extras:3.0.4.Final
org.jboss.security:jboss-negotiation-ntlm:3.0.4.Final
org.jboss.security:jboss-negotiation-spnego:3.0.4.Final
org.jboss.security:jbossxacml:2.0.8.Final
org.picketbox:picketbox-commons:1.0.0.final
org.picketlink:picketlink-api:2.5.5.SP12
org.picketlink:picketlink-common:2.5.5.SP12
org.picketlink:picketlink-federation:2.5.5.SP12
org.picketlink:picketlink-impl:2.5.5.SP12
org.picketlink:picketlink-idm-api:2.5.5.SP12
org.picketlink:picketlink-idm-impl:2.5.5.SP12
org.wildfly:wildfly-clustering-singleton-api:15.0.1.Final
org.wildfly:wildfly-security-api:15.0.1.Final
org.wildfly.security:wildfly-elytron:1.7.0.Final
org.wildfly.discovery:wildfly-discovery-client:1.1.1.Final
org.wildfly.client:wildfly-client-config:1.0.1.Final
org.wildfly.common:wildfly-common:1.4.0.Final
javax.activation:activation:1.1.1
javax.enterprise:cdi-api:2.0.SP1
javax.inject:javax.inject:1
javax.json:javax.json-api:1.1.2
javax.json.bind:javax.json.bind-api:1.0
javax.jws:jsr181-api:1.0-MR1
com.sun.mail:javax.mail:1.6.1
javax.persistence:javax.persistence-api:2.2
javax.security.enterprise:javax.security.enterprise-api:1.0
javax.validation:validation-api:2.0.1.Final
org.jboss.spec.javax.annotation:jboss-annotations-api_1.3_spec:1.0.1.Final
org.jboss.spec.javax.batch:jboss-batch-api_1.0_spec:1.0.1.Final
org.jboss.spec.javax.ejb:jboss-ejb-api_3.2_spec:1.0.1.Final
org.jboss.spec.javax.el:jboss-el-api_3.0_spec:1.0.12.Final
org.jboss.spec.javax.enterprise.concurrent:jboss-concurrency-api_1.0_spec:1.0.2.Final
org.jboss.spec.javax.faces:jboss-jsf-api_2.3_spec:2.3.5.SP1
org.jboss.spec.javax.interceptor:jboss-interceptors-api_1.2_spec:1.0.1.Fina
org.jboss.spec.javax.management.j2ee:jboss-j2eemgmt-api_1.1_spec:1.0.2.Final
org.jboss.spec.javax.resource:jboss-connector-api_1.7_spec:1.0.1.Final
org.jboss.spec.javax.rmi:jboss-rmi-api_1.0_spec:1.0.6.Final
org.jboss.spec.javax.security.jacc:jboss-jacc-api_1.5_spec:1.0.2.Final
org.jboss.spec.javax.security.auth.message:jboss-jaspi-api_1.1_spec:1.0.2.Final
org.jboss.spec.javax.jms:jboss-jms-api_2.0_spec:1.0.2.Final
org.jboss.spec.javax.servlet:jboss-servlet-api_4.0_spec:1.0.0.Final
org.jboss.spec.javax.servlet.jsp:jboss-jsp-api_2.3_spec:1.0.3.Final
org.apache.taglibs:taglibs-standard-spec:1.2.6-RC1
org.apache.taglibs:taglibs-standard-impl:1.2.6-RC1
org.jboss.spec.javax.transaction:jboss-transaction-api_1.2_spec:1.1.1.Final
org.jboss.spec.javax.websocket:jboss-websocket-api_1.1_spec:1.1.3.Final
org.jboss.spec.javax.ws.rs:jboss-jaxrs-api_2.1_spec:1.0.1.Final
org.jboss.spec.javax.xml.bind:jboss-jaxb-api_2.3_spec:1.0.1.Final
org.jboss.spec.javax.xml.soap:jboss-saaj-api_1.3_spec:1.0.6.Final
org.jboss.spec.javax.xml.ws:jboss-jaxws-api_2.3_spec:1.0.0.Final
```

> mvn help:effective-pom # zeigt die konkrete Liste für eine wildfy.version

Diese Artifakte werden alle vom Wildfly `provided`. Maven POMs die diese Abhänigkeiten impotieren, 
können diese potentiell als `<dependency>` erklären. Um das zu tun, sollte man
das entsprechende Artifakt `ohne <version>`, mit `<scope>provided</scope>` als dependency definieren.

```xml
<!-- JAX-RS annotations and interfaces --> 
<dependency>
    <groupId>org.jboss.spec.javax.ws.rs</groupId>
    <artifactId>jboss-jaxrs-api_2.1_spec</artifactId>
    <scope>provided</scope>
</dependency>

<!-- JAX-WS annotations and interfaces --> 
<dependency>
    <groupId>org.jboss.spec.javax.xml.ws</groupId>
    <artifactId>jboss-jaxws-api_2.3_spec</artifactId>
    <scope>provided</scope>
</dependency>
```  

### Compilation failure: package javax.xml.bind does not exist

Mit java11 macht Oracle ernst und schmeisst einige EE Module aus dem SE/JDK raus. Eines
der wichtigsten ist `javax.xml.bind` (JAXB). Somit kommt es oft zu compile Errors. 

```bash
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.1:compile\
  (default-compile) on project name: Compilation failure: Compilation failure:
[ERROR] Path/ToClass.java:[12,22] package javax.xml.bind does not exist
```

* [stackoverflow](https://stackoverflow.com/questions/52502189/java-11-package-javax-xml-bind-does-not-exist)
* [details](http://openjdk.java.net/jeps/320#Java-EE-modules)

> Fix it with these dependency
> ```xml
> <dependency>
>    <groupId>org.jboss.spec.javax.xml.bind</groupId>
>    <artifactId>jboss-jaxb-api_2.3_spec</artifactId>
>    <scope>provided</scope>
> </dependency>
> ```
