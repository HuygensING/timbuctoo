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
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Inject;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentReader implements MessageBodyReader<Document> {

  @Context
  private UriInfo uriInfo;
  @Context
  private Request request;

  @Inject
  private DocTypeRegistry docTypeRegistry;
  @Inject
  private JacksonJsonProvider jsonProvider;
  @Inject
  private Validator validator;

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(Document.class) && mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Document readFrom(Class<Document> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {

    String entityType = uriInfo.getPathParameters().getFirst("entityType");
    Class<?> cls = docTypeRegistry.getClassFromWebServiceTypeString(entityType);
    if (cls == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    Document doc = null;

    try {
      doc = (Document) jsonProvider.readFrom((Class<Object>) cls, cls, annotations, mediaType, httpHeaders, entityStream);
    } catch (IllegalArgumentException ex) {
      ex.printStackTrace();
    }

    if (doc == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    Set<ConstraintViolation<Document>> validationErrors = validator.validate(doc);

    //If we are posting a document we don't is some not null fields missing a value, these fields are possibly auto generated.
    if (!validationErrors.isEmpty() && !"POST".equals(request.getMethod())) {
      throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(validationErrors).type(MediaType.APPLICATION_JSON_TYPE).build());
    }
    return doc;
  }

}
