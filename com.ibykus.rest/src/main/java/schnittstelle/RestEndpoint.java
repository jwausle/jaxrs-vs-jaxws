package schnittstelle;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * JAX-RS rest endpoint
 * <pre>
 *   GET /pojo 'Accept: application/json' -> [JSON]
 *   GET /pojo 'Accept: application/xml'  -> [XML]
 *   GET /pojo/{id} 'Accept: application/json' -> JSON
 *   GET /pojo/{id} 'Accept: application/xml'  -> XML
 *
 *   PUT /pojo 'ContentType: application/json' 'Accept: application/json' JSON -> JSON
 *   PUT /pojo 'ContentType: application/json' 'Accept: application/xml' JSON  -> XML
 *   PUT /pojo 'ContentType: application/xml' 'Accept: application/json' XML   -> JSON
 *   PUT /pojo 'ContentType: application/xml' 'Accept: application/xml' XML    -> XML
 *
 *   POST /pojo 'ContentType: application/json' 'Accept: application/json' JSON -> JSON
 *   POST /pojo 'ContentType: application/json' 'Accept: application/xml' JSON  -> XML
 *   POST /pojo 'ContentType: application/xml' 'Accept: application/json' XML   -> JSON
 *   POST /pojo 'ContentType: application/xml' 'Accept: application/xml' XML    -> XML
 *
 *   POST /pojo/{id} 'ContentType: application/json' 'Accept: application/json' JSON -> JSON
 *   POST /pojo/{id} 'ContentType: application/json' 'Accept: application/xml' JSON  -> XML
 *   POST /pojo/{id} 'ContentType: application/xml' 'Accept: application/json' XML   -> JSON
 *   POST /pojo/{id} 'ContentType: application/xml' 'Accept: application/xml' XML    -> XML
 *
 *   DELETE /pojo/{id} 'Accept: application/json' -> JSON
 *   DELETE /pojo/{id} 'Accept: application/xml'  -> XML
 * </pre>
 */
@Path("/pojo")
public class RestEndpoint {
    private static final String NULL_ID_TO_CREATE_NEW_POJO = null;

    private final Map<String, Pojo> dataCache = new ConcurrentHashMap<>();

    @EJB
    private Ejb ejb;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PojoList httpGet() {
        return new PojoList(dataCache.values());
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Pojo httpGet(@PathParam("id") String id) {
        return dataCache.get(id);
    }

    @POST
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Pojo httpPost(@PathParam("id") String idOrNull, Pojo requestBody) {
        Objects.requireNonNull(requestBody, "requestBody");

        final Pojo localPojo = requestBody;
        final Optional<String> dataId = Optional.ofNullable(requestBody.getId());

        if (idOrNull == NULL_ID_TO_CREATE_NEW_POJO || !dataId.isPresent()) {
            localPojo.generateAndSetId();
        }

        dataCache.put(localPojo.getId(), localPojo);

        return localPojo;
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Pojo httpPut(Pojo requestBody) {
        return httpPost(requestBody);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Pojo httpPost(Pojo requestBody) {
        return httpPost(NULL_ID_TO_CREATE_NEW_POJO, requestBody);
    }

    @DELETE
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Pojo httpDelete(@PathParam("id") String id) {
        return dataCache.remove(id);
    }
}
