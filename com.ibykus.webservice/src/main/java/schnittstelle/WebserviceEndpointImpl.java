package schnittstelle;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.jws.WebService;

/**
 * JAX-WS impl of {@link WebserviceEndpoint} interface.
 */
@WebService(endpointInterface = WebserviceEndpoint.WEBSERVICE_INTERFACE, serviceName = WebserviceEndpoint.WEBSERVICE_NAME)
public class WebserviceEndpointImpl implements WebserviceEndpoint {
    private final Map<String, Pojo> dataCache = new ConcurrentHashMap<>();

    public WebserviceEndpointImpl() {
    }

    @Override
    public Pojo create() {
        return update(null);
    }

    @Override
    public PojoList readAll() {
        return new PojoList(dataCache.values());
    }

    @Override
    public Pojo read(String id) {
        return dataCache.get(id);
    }

    @Override
    public Pojo update(Pojo pojoOrNull) {
        final Pojo localPojo = Optional.ofNullable(pojoOrNull).orElse(new Pojo());
        final Optional<String> dataId = Optional.ofNullable(localPojo.getId());

        if (!dataId.isPresent()) {
            localPojo.generateAndSetId();
        }

        dataCache.put(localPojo.getId(), localPojo);

        return localPojo;
    }

    @Override
    public Pojo delete(String id) {
        return dataCache.remove(id);
    }
}
