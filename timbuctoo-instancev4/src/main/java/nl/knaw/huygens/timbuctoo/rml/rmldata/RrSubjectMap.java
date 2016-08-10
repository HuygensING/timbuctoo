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
      /*
      If the term map does not have a rr:termType property, then its term type is:

        rr:Literal, if it is an object map and at least one of the following conditions is true:
          [..SNIP...]
        rr:IRI, otherwise.
       */
      instance.termMap = new RrColumn(referenceString, TermType.IRI);
      return this;
    }

    public Builder withColumnTerm(String referenceString, TermType type) {
      //assert termtype is IRI or blanknode
      instance.termMap = new RrColumn(referenceString, type);
      return this;
    }

    public Builder withConstantTerm(Node value) {
      instance.termMap = new RrConstant(value);
      return this;
    }

    public Builder withTemplateTerm(String templateString) {
      /*
      If the term map does not have a rr:termType property, then its term type is:

        rr:Literal, if it is an object map and at least one of the following conditions is true:
          [..SNIP...]
        rr:IRI, otherwise.
       */
      instance.termMap = new RrColumn(templateString, TermType.IRI);
      return this;
    }

    public Builder withTemplateTerm(String templateString, TermType type) {
      //assert termtype is IRI or blanknode
      instance.termMap = new RrTemplate(templateString, type);
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
