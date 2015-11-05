package nl.knaw.huygens.timbuctoo.tools.conversion;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import com.tinkerpop.blueprints.Vertex;

public class VariationConverter {

  private ElementConverterFactory converterFactory;

  public VariationConverter(TypeRegistry typeRegistry) {
    this(new ElementConverterFactory(typeRegistry));
  }

  VariationConverter(ElementConverterFactory converterFactory) {
    this.converterFactory = converterFactory;
  }

  public <T extends DomainEntity> void addDataToVertex(Vertex vertex, T variant) throws ConversionException {
    getConverter(variant).addValuesToElement(vertex, variant);
  }

  @SuppressWarnings("unchecked")
  private <T extends DomainEntity> VertexConverter<T> getConverter(T variant) {
    return converterFactory.forType((Class<T>) variant.getClass());
  }

}
