package schnittstelle;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link RestEndpoint}
 */
public class RestEndpointTest {
    private RestEndpoint testSubject;

    @Before
    public void beforeTest() {
        testSubject = new RestEndpoint();
    }

    @Test
    public void httpGetDefaultTest() {
        final PojoList expectedPojos = testSubject.httpGet();
        Assert.assertNotNull(expectedPojos);
        Assert.assertEquals(0, expectedPojos.getPojos().size());
    }

    @Test
    public void httpPutTest() {
        final Pojo pojo = testSubject.httpPut(new Pojo(5));
        Assert.assertNotNull(pojo);
        Assert.assertNotNull(pojo.getId());
        Assert.assertEquals(Integer.valueOf(5), pojo.getValue());
    }

    @Test
    public void httpPostTest() {
        final Pojo pojo = testSubject.httpPost(new Pojo(5));
        Assert.assertNotNull(pojo);
        Assert.assertNotNull(pojo.getId());
        Assert.assertEquals(Integer.valueOf(5), pojo.getValue());
    }

    @Test
    public void httpCRUDTest() {
        final Pojo pojo = testSubject.httpPost(new Pojo(5));
        Assert.assertNotNull(pojo);
        Assert.assertNotNull(pojo.getId());
        Assert.assertEquals(Integer.valueOf(5), pojo.getValue());

        final PojoList expectedPojos = testSubject.httpGet();
        Assert.assertNotNull(expectedPojos);
        Assert.assertEquals(1, expectedPojos.getPojos().size());

        final Pojo actualPojo = expectedPojos.getPojos().get(0);
        final Pojo updatePojo = new Pojo(actualPojo.getId(), actualPojo.getValue() + 1);

        final Pojo actualPojo2 = testSubject.httpPost(updatePojo.getId(), updatePojo);
        Assert.assertEquals(updatePojo, actualPojo2);

        final PojoList expectedPojos2 = testSubject.httpGet();
        Assert.assertNotNull(expectedPojos);
        Assert.assertEquals(1, expectedPojos.getPojos().size());

        final Pojo actualPojo3 = testSubject.httpDelete(updatePojo.getId());
        Assert.assertEquals(updatePojo, actualPojo3);

        final PojoList expectedPojos3 = testSubject.httpGet();
        Assert.assertNotNull(expectedPojos3);
        Assert.assertEquals(0, expectedPojos3.getPojos().size());
    }
}
