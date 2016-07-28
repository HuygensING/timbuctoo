package nl.knaw.huygens.timbuctoo.rml.rmldata;


import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrColumn;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrConstant;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTemplate;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTermMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.TermType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_URI;

import java.util.function.Consumer;

import static nl.knaw.huygens.timbuctoo.rml.rmldata.RrPredicateObjectMap.rrPredicateObjectMap;

public class RrSubjectMap {
  private RrTermMap termMap;

  public RrTermMap getTermMap() {
    return termMap;
  }

  public static Builder rrSubjectMap() {
    return new Builder();
  }

  public static class Builder {
    private final RrSubjectMap instance;
    private Node_URI className;

    public Builder() {
      this.instance = new RrSubjectMap();
    }

    public Builder withColumnTerm(String referenceString) {
      instance.termMap = new RrColumn(false, referenceString);
      return this;
    }

    public Builder withColumnTerm(String referenceString, TermType type) {
      instance.termMap = new RrColumn(false, referenceString, type);
      return this;
    }

    public Builder withConstantTerm(Node value) {
      instance.termMap = new RrConstant(value);
      return this;
    }

    public Builder withTemplateTerm(String templateString) {
      instance.termMap = new RrTemplate(templateString);
      return this;
    }

    public Builder withClass(Node_URI className) {
      this.className = className;
      return this;
    }

    RrSubjectMap build(Consumer<RrPredicateObjectMap.Builder> consumer) {
      if (this.className != null) {
        consumer.accept(rrPredicateObjectMap()
          .withConstant(this.className)
          .withPredicate((Node_URI) NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
        );
      }
      return instance;
    }
  }
}
