package nl.knaw.huygens.timbuctoo.rest.providers;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.rest.resources.DomainEntityResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Inject;

/**
 * A {@code Provider} that converts a stream to a (@code Document} instance.
 * Note that the request path parameter {@code DomainEntityResource.ENTITY_PARAM}
 * is used, which contains an external document type name, e.g., "persons".
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class DomainEntityReader implements MessageBodyReader<Entity> {

  private final Logger LOG = LoggerFactory.getLogger(DomainEntityReader.class);

  @Context
  private UriInfo uriInfo;
  @Context
  private Request request;

  @Inject
  private TypeRegistry typeRegistry;
  @Inject
  private JacksonJsonProvider jsonProvider;
  @Inject
  private Validator validator;

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return DomainEntity.class.isAssignableFrom(type) && mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Entity readFrom(Class<Entity> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {

    String entityType = uriInfo.getPathParameters().getFirst(DomainEntityResource.ENTITY_PARAM);
    if (entityType == null) {
      throw new TimbuctooException(Status.NOT_FOUND, "Missing path parameter");
    }
    Class<?> cls = typeRegistry.getTypeForXName(entityType);
    if (cls == null) {
      throw new TimbuctooException(Status.NOT_FOUND, String.format("Unknown document type %s", entityType));
    }

    Entity doc = null;

    try {
      doc = (Entity) jsonProvider.readFrom((Class<Object>) cls, cls, annotations, mediaType, httpHeaders, entityStream);
    } catch (IllegalArgumentException e) {
      LOG.error(e.getMessage());
    }

    if (doc == null) {
      throw new TimbuctooException(Status.BAD_REQUEST, String.format("Failed to convert entity type %s", entityType));
    }

    Set<ConstraintViolation<Entity>> validationErrors = validator.validate(doc);

    // If we are posting a document we don't is some not null fields missing a value, these fields are possibly auto generated.
    if (!validationErrors.isEmpty() && !"POST".equals(request.getMethod())) {
      throw new TimbuctooException(Status.BAD_REQUEST, String.format("Validation errors for type %s", entityType), validationErrors.toString());
    }
    return doc;
  }

}
