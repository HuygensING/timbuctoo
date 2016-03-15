package nl.knaw.huygens.timbuctoo.rest.providers;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Inject;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter;
import nl.knaw.huygens.facetedsearch.serialization.FacetParameterDeserializer;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.rest.util.serialization.ChangeSerializer;
import nl.knaw.huygens.timbuctoo.security.UserConfigurationHandler;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
  private ObjectMapper mapper;

  @Inject
  public ObjectMapperContextResolver(UserConfigurationHandler users) {
    mapper = new ObjectMapper();
    // Helpers for serializing and deserializing enums.
    mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

    mapper.registerModule(createFacetedSearchModule());
    mapper.registerModule(createTimbuctooModule(users));
  }



  private Module createTimbuctooModule(UserConfigurationHandler users) {
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(Change.class, new ChangeSerializer(users));
    return simpleModule;
  }

  private Module createFacetedSearchModule() {
    SimpleModule module = new SimpleModule();
    // Helper for deserializing FacetParameters
    module.addDeserializer(FacetParameter.class, new FacetParameterDeserializer());
    return module;
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return mapper;
  }
}
