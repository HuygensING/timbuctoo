package nl.knaw.huygens.timbuctoo.rest.providers;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter;
import nl.knaw.huygens.facetedsearch.serialization.FacetParameterDeserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Provider
public class ObjectMapperContext implements ContextResolver<ObjectMapper> {
  private ObjectMapper mapper;

  public ObjectMapperContext() {
    mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(FacetParameter.class, new FacetParameterDeserializer());
    mapper.registerModule(module);
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return mapper;
  }
}
