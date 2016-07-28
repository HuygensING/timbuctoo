package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrColumn;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrConstant;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTemplate;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTermMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.TermType;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.referencingobjectmaps.RrRefObjectMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_URI;

public class RrPredicateObjectMap {
  private Node_URI predicate;
  private RrTermMap objectMap;

  public RrPredicateObjectMap() {
  }

  public Node_URI getPredicate() {
    return predicate;
  }

  public RrTermMap getObjectMap() {
    return objectMap;
  }

  public static Builder rrPredicateObjectMap() {
    return new Builder();
  }

  public static class Builder {
    private final RrPredicateObjectMap instance;
    private RrRefObjectMap.Builder objectMapBuilder;

    public Builder() {
      this.instance = new RrPredicateObjectMap();
    }

    public Builder withPredicate(Node_URI predicate) {
      instance.predicate = predicate;
      return this;
    }

    public Builder withColumn(String referenceString) {
      instance.objectMap = new RrColumn(true, referenceString);
      return this;
    }

    public Builder withColumn(String referenceString, TermType type) {
      instance.objectMap = new RrColumn(true, referenceString, type);
      return this;
    }

    public Builder withConstant(Node value) {
      instance.objectMap = new RrConstant(value);
      return this;
    }

    public Builder withTemplate(String templateString) {
      instance.objectMap = new RrTemplate(templateString);
      return this;
    }

    public Builder withReference(RrRefObjectMap.Builder subBuilder) {
      this.objectMapBuilder = subBuilder;
      return this;
    }
    
    public RrRefObjectMap.Builder withReference() {
      this.objectMapBuilder = new RrRefObjectMap.Builder();
      return this.objectMapBuilder;
    }

    RrPredicateObjectMap build(RrTriplesMap parentTriplesMap) {
      if (instance.objectMap == null) {
        instance.objectMap = objectMapBuilder.build();
      }
      return this.instance;
    }
  }
}
