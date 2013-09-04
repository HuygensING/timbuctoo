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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.rest.resources.RESTAutoResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Inject;

/**
 * A {@code Provider} that converts a stream to a (@code Document} instance.
 * Note that the request path parameter {@code RESTAutoResource.ENTITY_PARAM}
 * is used, which contains an external document type name, e.g., "persons".
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentReader implements MessageBodyReader<Document> {

  private final Logger LOG = LoggerFactory.getLogger(DocumentReader.class);

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

    String entityType = uriInfo.getPathParameters().getFirst(RESTAutoResource.ENTITY_PARAM);
    if (entityType == null) {
      LOG.error("Missing path parameter '{}'", RESTAutoResource.ENTITY_PARAM);
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    Class<?> cls = docTypeRegistry.getTypeForXName(entityType);
    if (cls == null) {
      LOG.error("Cannot convert '{}' to a document type", entityType);
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    Document doc = null;

    try {
      doc = (Document) jsonProvider.readFrom((Class<Object>) cls, cls, annotations, mediaType, httpHeaders, entityStream);
    } catch (IllegalArgumentException e) {
      LOG.error(e.getMessage());
    }

    if (doc == null) {
      LOG.error("Failed to convert JSON for document with entity type {}", entityType);
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    Set<ConstraintViolation<Document>> validationErrors = validator.validate(doc);

    //If we are posting a document we don't is some not null fields missing a value, these fields are possibly auto generated.
    if (!validationErrors.isEmpty() && !"POST".equals(request.getMethod())) {
      LOG.error("Validation error(s) for document with entity type {}", entityType);
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(validationErrors).type(MediaType.APPLICATION_JSON_TYPE).build());
    }
    return doc;
  }

}
