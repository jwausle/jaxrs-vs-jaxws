package schnittstelle;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Objects;

import javax.ws.rs.NotAcceptableException;

import helper.ResteasyServerFacade;

/**
 * Integration test for {@link RestEndpoint}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RestEndpointITest {
    private static final int RANDOM_PORT = 0;
    private static final Pojo TEST_POJO_WITHOUT_ID = new Pojo(5);

    private ResteasyServerFacade resteasyFacade;

    @Mock
    private Ejb ejbMock;

    @InjectMocks
    private final RestEndpoint restEndpoint = new RestEndpoint();

    @Before
    public void beforeTest() {
        final ResteasyServerFacade newResteasy = ResteasyServerFacade.builder()
                .port(RANDOM_PORT)
                .configureApplication(new RestApplication())
                .configureProvider(new ObjectMapperJsonProvider())
                .configureProvider(new ObjectMapperXmlProvider())
                .configureResources(restEndpoint)
                .build();
        this.resteasyFacade = Objects.requireNonNull(newResteasy, "newResteasy");
        this.resteasyFacade.startServer();

//    final CDI<Object> current = CDI.current();
//    System.out.println(current);
    }

    @After
    public void afterTest() {
        resteasyFacade.teardownServer();
    }

    @Test
    public void assertThat_unknownId_returnNull() {
        Object actual = resteasyFacade.request("/rest/pojo/unknown_id")
                .headerAcceptJson()
                .httpGet(Pojo.class);
        Assert.assertNull(actual);
    }

    @Test
    public void assertThat_wrongAcceptType_response406() {
        Assertions.assertThatThrownBy(() -> resteasyFacade.request("/rest/pojo/unknown_id")
                .headerAcceptText(/* wrong 'Accept: testPortSetting/plain' force 406 */)
                .httpGet(Pojo.class))
                .describedAs("Expect that GET http/rest/pojo/unknown_id 'Accept:text/plain' fail.")
                .isInstanceOf(NotAcceptableException.class)
                .describedAs("Expect that GET http/rest/pojo/unknown_id 'Accept:text/plain' fail with 406")
                .hasMessage("HTTP 406 Not Acceptable");
    }

    @Test
    public void assertThat_put_createNewPojo() {
        Assertions.assertThat(resteasyFacade.request("/rest/pojo")
                .dataJson(TEST_POJO_WITHOUT_ID)
                .headerAcceptJson()
                .httpPut(Pojo.class))
                .describedAs("Expect that PUT http/rest/pojo response a new Pojo")
                .matches(pojo -> pojo != null)
                .describedAs("Expect that PUT http/rest/pojo response a new Pojo with id")
                .matches(pojo -> pojo.getId() != null)
                .describedAs("Expect that PUT http/rest/pojo response a new Pojo with value==5")
                .matches(pojo -> pojo.getValue() == TEST_POJO_WITHOUT_ID.getValue());
    }

    @Test
    public void assertThat_post_createNewPojo() {
        Assertions.assertThat(resteasyFacade.request("/rest/pojo")
                .dataJson(TEST_POJO_WITHOUT_ID)
                .headerAcceptJson()
                .httpPost(Pojo.class))
                .describedAs("Expect that POST http/rest/pojo response a new schnittstelle.Pojo")
                .matches(pojo -> pojo != null)
                .describedAs("Expect that POST http/rest/pojo response a new schnittstelle.Pojo with id")
                .matches(pojo -> pojo.getId() != null)
                .describedAs("Expect that POST http/rest/pojo response a new schnittstelle.Pojo with value==5")
                .matches(pojo -> pojo.getValue() == TEST_POJO_WITHOUT_ID.getValue());
    }


    @Test
    public void assertThat_CRUD_success() {
        Assertions.assertThat(resteasyFacade.request("/rest/pojo")
                .headerAcceptJson()
                .httpGet(PojoList.class))
                .describedAs("Expect that GET http/rest/pojo response []")
                .matches(pojos -> pojos != null)
                .describedAs("Expect that GET http/rest/pojo response [] list")
                .matches(pojos -> pojos.getPojos().isEmpty());

        final Pojo pojo = resteasyFacade.request("/rest/pojo")
                .dataJson(TEST_POJO_WITHOUT_ID)
                .headerAcceptJson()
                .httpPut(Pojo.class);

        final String id = pojo.getId();
        final Integer value = pojo.getValue();
        Assertions.assertThat(resteasyFacade.request("/rest/pojo")
                .headerAcceptJson()
                .httpGet(PojoList.class))
                .describedAs("Expect that GET http/rest/pojo response [not null]")
                .matches(pojos -> pojos != null)
                .describedAs("Expect that GET http/rest/pojo response [].size==1")
                .matches(pojos -> pojos.getPojos().size() == 1)
                .describedAs("Expect that GET http/rest/pojo response [].first()==pojo")
                .matches(pojos -> pojos.getPojos().get(0).equals(pojo))
        ;

        final Pojo updatePojo = new Pojo(id, value + 1);
        Assertions.assertThat(resteasyFacade.request("/rest/pojo/" + id)
                .dataJson(updatePojo)
                .headerAcceptJson()
                .httpPost(Pojo.class))
                .describedAs("Expect that POST http/rest/pojo/1 response [].first()==updatePojo")
                .isEqualTo(updatePojo);

        Assertions.assertThat(resteasyFacade.request("/rest/pojo")
                .headerAcceptJson()
                .httpGet(PojoList.class))
                .describedAs("Expect that GET http/rest/pojo response [not null]")
                .matches(pojos -> pojos != null)
                .describedAs("Expect that GET http/rest/pojo response [].size==1")
                .matches(pojos -> pojos.getPojos().size() == 1)
                .describedAs("Expect that GET http/rest/pojo response [].first()==updatePojo")
                .matches(pojos -> pojos.getPojos().get(0).equals(updatePojo));

        Assertions.assertThat(resteasyFacade.request("/rest/pojo/" + id)
                .headerAcceptJson()
                .httpDelete(Pojo.class))
                .describedAs("Expect that DELETE http/rest/pojo/1 response updatePojo")
                .isEqualTo(updatePojo);

        Assertions.assertThat(resteasyFacade.request("/rest/pojo")
                .headerAcceptJson()
                .httpGet(PojoList.class))
                .describedAs("Expect that GET http/rest/pojo response [not null]")
                .matches(pojos -> pojos != null)
                .describedAs("Expect that GET http/rest/pojo response [].isEmpty")
                .matches(pojos -> pojos.getPojos().isEmpty());
    }
}
