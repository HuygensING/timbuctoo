package nl.knaw.huygens.repository.providers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Inject;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;

@Provider
@Consumes(MediaType.APPLICATION_JSON) 
public class DocumentReader implements MessageBodyReader<Document> {
  @Context
  private UriInfo uriInfo;
  
  @Inject
  private DocumentTypeRegister docTypeRegistry;
  @Inject
  private JacksonJsonProvider jsonProvider;
  @Inject
  private Validator validator;

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(Document.class) && mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Document readFrom(Class<Document> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    String entityType = uriInfo.getPathParameters().getFirst("entityType");
    Class<?> cls = docTypeRegistry.getClassFromTypeString(entityType);
    Document doc = (Document) jsonProvider.readFrom((Class<Object>) cls, cls, annotations, mediaType, httpHeaders, entityStream);
    Set<ConstraintViolation<Document>> validationErrors = validator.validate(doc);
    if (!validationErrors.isEmpty()) {
      throw new WebApplicationException(Response.status(400).entity(validationErrors).type(MediaType.APPLICATION_JSON_TYPE).build());
    }
    return doc;
  }

}
