package schnittstelle;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.SecureRandom;

import helper.CxfServerFacade;

/**
 * Integration test of {@link WebserviceEndpointImpl}
 */
public class WebserviceEndpointImplITest {
    private final WebserviceEndpointImpl testSubject = new WebserviceEndpointImpl();

    private final CxfServerFacade cxfFacade = CxfServerFacade.builder()
            .wsInstance(testSubject)
            .wsInterface(WebserviceEndpoint.class)
            .wsServiceQName(WebserviceEndpoint.WEBSERVICE_QNAME)
            .localhost()
            .randomPort()
            .build();

    @Before
    public void beforeTest() {
        cxfFacade.startServer();
    }

    @After
    public void afterTest() {
        cxfFacade.teardownServer();
    }

    @Test
    public void assertThat_CR_responseEqualPojos() {
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

    @Test
    public void assertThat_CD_responseNoPojos() {
        final WebserviceEndpoint proxy = WebserviceClient.builder()
                .endpoint(cxfFacade.getUrl())
                .webservice(WebserviceEndpoint.class)
                .build()
                .proxy(WebserviceEndpoint.class);

        final Pojo pojo = proxy.create();
        Assert.assertNotNull(pojo);
        Assert.assertEquals(1, proxy.readAll().getPojos().size());

        proxy.delete(pojo.getId());
        Assert.assertEquals(0, proxy.readAll().getPojos().size());
    }

    @Test
    public void assertThat_CU_responseUpdatedPojo() {
        final WebserviceEndpoint proxy = WebserviceClient.builder()
                .endpoint(cxfFacade.getUrl())
                .webservice(WebserviceEndpoint.class)
                .build()
                .proxy(WebserviceEndpoint.class);

        final Pojo pojo = proxy.create();
        Assert.assertEquals(1, proxy.readAll().getPojos().size());

        final int updatedValue = new SecureRandom().nextInt();
        pojo.setValue(updatedValue);
        final Pojo updated = proxy.update(pojo);
        Assert.assertEquals(1, proxy.readAll().getPojos().size());
    }

    @Test
    public void assertThat_CRUD_responseWell() {
        final WebserviceEndpoint proxy = WebserviceClient.builder()
                .endpoint(cxfFacade.getUrl())
                .webservice(WebserviceEndpoint.class)
                .build()
                .proxy(WebserviceEndpoint.class);

        final Pojo pojo = proxy.create();
        Assert.assertEquals(1, proxy.readAll().getPojos().size());

        final Pojo read = proxy.read(pojo.getId());
        Assert.assertEquals(pojo, read);

        final int updatedValue = new SecureRandom().nextInt();
        read.setValue(updatedValue);

        final Pojo updated = proxy.update(read);
        Assert.assertEquals(updatedValue, updated.getValue().intValue());

        proxy.delete(updated.getId());
        Assert.assertEquals(0, proxy.readAll().getPojos().size());
    }
}
