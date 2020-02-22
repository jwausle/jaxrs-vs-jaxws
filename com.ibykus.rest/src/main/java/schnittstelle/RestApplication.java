package schnittstelle;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS application to instantiate the rest endpoint.
 */
@ApplicationPath("/rest")
public class RestApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Collections.singleton(RestEndpoint.class);
    }

    @Override
    public Set<Object> getSingletons() {
        final LinkedHashSet<Object> singletons = new LinkedHashSet<>();
        singletons.add(new ObjectMapperJsonProvider());
        singletons.add(new ObjectMapperXmlProvider());
        return singletons;
    }
}
