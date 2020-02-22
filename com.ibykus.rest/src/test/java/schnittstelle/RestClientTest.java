package schnittstelle;

import org.assertj.core.api.Assertions;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Client;

/**
 * Unit test of {@link RestClient}, {@link RestClient.Request} and {@link RestClient.RestClientBuilder}
 */
public class RestClientTest {
    private static final String JSON = "{'json':'dummy'}".replace("'", "\"");

    private final RestClient.RestClientBuilder testSubject = RestClient.builder();

    @Test
    public void defaultBuildTest() {
        Assertions.assertThat(testSubject.buildRestClient())
                .describedAs("InstanceOf schnittstelle.RestClient, spec.Client and ResteasyClientBuilder")
                .isInstanceOf(RestClient.class);

        Assertions.assertThat(testSubject.build())
                .describedAs("InstanceOf spec.Client and ResteasyClientBuilder")
                .isInstanceOf(ResteasyClient.class)
                .isInstanceOf(Client.class);

        final RestClient.Request actual = testSubject.buildRestClient().httpRequest();
        Assert.assertEquals("http://localhost:80/ -H 'ContentType: text/plain' -H 'Accept: []' --data Optional.empty", actual.toString());
    }

    @Test
    public void testPortSetting() {
        final RestClient.Request actual = testSubject.port(8080)
                .buildRestClient()
                .httpRequest();
        Assert.assertEquals("http://localhost:8080/ -H 'ContentType: text/plain' -H 'Accept: []' --data Optional.empty", actual.toString());
    }

    @Test
    public void testHostSetting() {
        final RestClient.Request actual = testSubject.host("host")
                .buildRestClient()
                .httpRequest();
        Assert.assertEquals("http://host:80/ -H 'ContentType: text/plain' -H 'Accept: []' --data Optional.empty", actual.toString());
    }

    @Test
    public void testPathSetting() {
        final RestClient.Request actual = testSubject
                .buildRestClient()
                .httpRequest("path");
        Assert.assertEquals("http://localhost:80/path -H 'ContentType: text/plain' -H 'Accept: []' --data Optional.empty", actual.toString());

        final RestClient.Request actual2 = testSubject
                .buildRestClient()
                .httpRequest()
                .path("path");
        Assert.assertEquals("http://localhost:80/path -H 'ContentType: text/plain' -H 'Accept: []' --data Optional.empty", actual2.toString());

        final RestClient.Request actual3 = testSubject
                .buildRestClient()
                .httpRequest()
                .addPath("path");
        Assert.assertEquals("http://localhost:80/path -H 'ContentType: text/plain' -H 'Accept: []' --data Optional.empty", actual3.toString());
    }

    @Test
    public void testPathAdding() {
        final RestClient.Request actual = testSubject
                .buildRestClient()
                .httpRequest("path/subpath");
        Assert.assertEquals("http://localhost:80/path/subpath -H 'ContentType: text/plain' -H 'Accept: []' --data Optional.empty", actual.toString());

        final RestClient.Request actual2 = testSubject
                .buildRestClient()
                .httpRequest()
                .addPath("path")
                .addPath("subpath");
        Assert.assertEquals("http://localhost:80/path/subpath -H 'ContentType: text/plain' -H 'Accept: []' --data Optional.empty", actual2.toString());
    }

    @Test
    public void testHttpsSetting() {
        final RestClient.Request actual = testSubject.buildRestClient()
                .httpsRequest();
        Assert.assertEquals("https://localhost:443/ -H 'ContentType: text/plain' -H 'Accept: []' --data Optional.empty", actual.toString());
    }

    @Test
    public void testHttpsSettingWithAnotherPort() {
        final RestClient.Request actual = testSubject.port(8443)
                .buildRestClient()
                .httpsRequest();
        Assert.assertEquals("https://localhost:8443/ -H 'ContentType: text/plain' -H 'Accept: []' --data Optional.empty", actual.toString());
    }

    @Test
    public void testAcceptHeaderSetting() {
        final RestClient.Request actual = testSubject
                .buildRestClient()
                .httpRequest()
                .headerAcceptJson();
        Assert.assertEquals("http://localhost:80/ -H 'ContentType: text/plain' -H 'Accept: [application/json]' --data Optional.empty", actual.toString());

        final RestClient.Request actual2 = testSubject
                .buildRestClient()
                .httpRequest()
                .headerAcceptJson()
                .headerAcceptXml();
        Assert.assertEquals("http://localhost:80/ -H 'ContentType: text/plain' -H 'Accept: [application/json, application/xml]' --data Optional.empty", actual2.toString());
    }

    @Test
    public void testContentTypeHeaderSetting() {
        final RestClient.Request actual = testSubject
                .buildRestClient()
                .httpRequest()
                .dataJson(null);
        Assert.assertEquals("http://localhost:80/ -H 'ContentType: application/json' -H 'Accept: []' --data Optional.empty", actual.toString());
    }

    @Test
    public void testBodySetting() {
        final RestClient.Request actual = testSubject
                .buildRestClient()
                .httpRequest()
                .dataJson(JSON);
        Assert.assertEquals("http://localhost:80/ -H 'ContentType: application/json' -H 'Accept: []' --data Optional[" + JSON + "]", actual.toString());
    }
}
