package schnittstelle;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.apache.commons.io.input.XmlStreamReader;
import org.apache.commons.io.output.XmlStreamWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * EE Provider to use this class as {@link MessageBodyReader} and {@link MessageBodyWriter} to support the mixin xml and json annotations for xml
 * (un)marshalling.
 */
@Provider
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class ObjectMapperXmlProvider implements ContextResolver<XmlMapper>, MessageBodyReader<Object>, MessageBodyWriter<Object> {
    private final List<Class> xmlPojos = Arrays.asList(Pojo.class);
    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public XmlMapper getContext(Class<?> type) {
        return xmlMapper;
    }

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return matchThis(aClass, mediaType);
    }

    @Override
    public Object readFrom(Class<Object> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
        XmlStreamReader xmlStreamReader = new XmlStreamReader(inputStream);
        final Object xml = xmlMapper.readValue(xmlStreamReader, aClass);
        return xml;
    }

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return matchThis(aClass, mediaType);
    }

    @Override
    public void writeTo(Object o, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
        xmlMapper.writeValue(new XmlStreamWriter(outputStream), o);
    }

    private boolean matchThis(Class<?> aClass, MediaType mediaType) {
        boolean isXmlMediaType = MediaType.APPLICATION_XML_TYPE.isCompatible(mediaType);
        boolean isXmlPojoClass = xmlPojos.contains(aClass);
        return isXmlMediaType && isXmlPojoClass;
    }
}
