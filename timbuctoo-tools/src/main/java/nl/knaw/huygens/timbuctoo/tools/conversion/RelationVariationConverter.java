package nl.knaw.huygens.timbuctoo.tools.conversion;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import com.tinkerpop.blueprints.Edge;

public class RelationVariationConverter {

  private ElementConverterFactory converterFactory;

  public RelationVariationConverter(TypeRegistry typeRegistry) {
    this(new ElementConverterFactory(typeRegistry));
  }

  RelationVariationConverter(ElementConverterFactory converterFactory) {
    this.converterFactory = converterFactory;
  }

  public <T extends Relation> void addToEdge(Edge edge, T variant) throws ConversionException {
    EdgeConverter<T> converter = getConverterFor(variant);
    converter.addValuesToElement(edge, variant);
  }

  @SuppressWarnings("unchecked")
  private <T extends Relation> EdgeConverter<T> getConverterFor(T variant) {
    return converterFactory.forRelation((Class<T>) variant.getClass());
  }

}
