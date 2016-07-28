package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrColumn;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrConstant;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTemplate;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTermMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.TermType;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.referencingobjectmaps.RrRefObjectMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Triple;

import java.util.Map;

public class RrPredicateObjectMap {
  private Node_URI predicate;
  private RrTermMap objectMap;

  public RrPredicateObjectMap() {
  }

  public static Builder rrPredicateObjectMap() {
    return new Builder();
  }

  public Triple generateValue(Node subject, Map<String, Object> stringObjectMap) {
    Node value = objectMap.generateValue(stringObjectMap);
    return new Triple(subject, predicate, value);
  }

  public static class Builder {
    private final RrPredicateObjectMap instance;
    private RrRefObjectMap.Builder referencingObjectMapBuilder;

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
      this.referencingObjectMapBuilder = subBuilder;
      return this;
    }
    
    public RrRefObjectMap.Builder withReference() {
      this.referencingObjectMapBuilder = new RrRefObjectMap.Builder();
      return this.referencingObjectMapBuilder;
    }

    RrPredicateObjectMap build(RrTriplesMap parentTriplesMap, DataSource dataSource) {
      if (instance.objectMap == null) {
        instance.objectMap = referencingObjectMapBuilder.build(parentTriplesMap, dataSource);
      }
      return this.instance;
    }

    public void fixupTripleMaps(TripleMapGetter getter) {
      if (referencingObjectMapBuilder != null) {
        referencingObjectMapBuilder.fixupTripleMaps(getter);
      }
    }
  }
}
