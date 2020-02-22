package schnittstelle;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test of {@link WebserviceEndpointImpl}.
 */
public class WebserviceEndpointImplTest {
    private final WebserviceEndpointImpl testSubject = new WebserviceEndpointImpl();

    @Test
    public void createTest() {
        final Pojo actual = testSubject.create();
        Assert.assertNotNull(actual);
        Assert.assertNotNull(actual.getId());
        Assert.assertNull(actual.getValue());
    }

    @Test
    public void getTest() {
        Assert.assertEquals(0, testSubject.readAll().getPojos().size());

        final Pojo actual = testSubject.create();

        Assert.assertEquals(1, testSubject.readAll().getPojos().size());

        Assert.assertEquals(actual, testSubject.read(actual.getId()));
    }

    @Test
    public void updateTest() {
        final Pojo actual = testSubject.create();
        Assert.assertNull(actual.getValue());

        actual.setValue(5);

        final Pojo update = testSubject.update(actual);
        Assert.assertNotNull(update.getValue());
        Assert.assertEquals(actual, update);
    }

    @Test
    public void deleteTest() {
        final Pojo actual = testSubject.create();
        Assert.assertEquals(1, testSubject.readAll().getPojos().size());

        testSubject.delete(actual.getId());
        Assert.assertEquals(0, testSubject.readAll().getPojos().size());
    }
}
