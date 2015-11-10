package nl.knaw.huygens.timbuctoo.rest.providers;

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
