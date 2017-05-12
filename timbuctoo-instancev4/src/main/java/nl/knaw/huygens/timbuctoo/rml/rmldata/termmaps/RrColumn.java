package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import nl.knaw.huygens.timbuctoo.rml.Row;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.Optional;

public class RrColumn implements RrTermMap {
  private final String referenceString;
  private TermType termType;
  private final RDFDatatype dataType;

  public RrColumn(String referenceString, TermType termType, RDFDatatype dataType) {
    this.referenceString = referenceString;
    this.termType = termType;
    this.dataType = dataType;
  }

  @Override
  public Optional<Node> generateValue(Row input) {
    String value = input.getRawValue(referenceString);
    if (value == null) {
      return Optional.empty();
    }

    switch (termType) {
      case IRI:
        return Optional.of(NodeFactory.createURI(value));
      case BlankNode:
        return Optional.of(NodeFactory.createBlankNode(value));
      case Literal:
        return Optional.of(NodeFactory.createLiteral(value, dataType));
      default:
        throw new UnsupportedOperationException("Not all items in the Enumerable where handled");
    }
  }

  @Override
  public String toString() {
    return String.format("      Column: %s (%s)\n",
      this.referenceString,
      this.termType
    );
  }

}
