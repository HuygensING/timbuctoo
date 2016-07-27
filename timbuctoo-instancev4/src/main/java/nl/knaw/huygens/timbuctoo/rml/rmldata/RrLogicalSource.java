package nl.knaw.huygens.timbuctoo.rml.rmldata;

import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_URI;

import java.util.Optional;

public class RrLogicalSource {
  public final Node_URI source;
  public final Node_Literal iterator;
  public final Optional<Node_Literal> referenceFormulation;


  public RrLogicalSource(Node_URI source, Node_Literal iterator, Node_Literal referenceFormulation) {
    this.source = source;
    this.iterator = iterator;
    this.referenceFormulation = Optional.of(referenceFormulation);
  }

  public RrLogicalSource(Node_URI source, Node_Literal iterator) {
    this.source = source;
    this.iterator = iterator;
    this.referenceFormulation = Optional.empty();
  }
}
