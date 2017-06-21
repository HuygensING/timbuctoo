package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import nl.knaw.huygens.timbuctoo.rml.Row;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.Optional;

public class RrConstant implements RrTermMap {
  private final Node value;

  public RrConstant(String value) {
    this.value = NodeFactory.createURI(value);
  }

  public RrConstant(String value, TermType termType, RDFDatatype dataType) {
    if (termType == TermType.Literal) {
      this.value = NodeFactory.createLiteral(value, dataType);
    } else {
      this.value = NodeFactory.createURI(value);
    }
  }

  @Override
  public Optional<Node> generateValue(Row input) {
    return Optional.of(value);
  }

  @Override
  public String toString() {
    return String.format("      Constant: %s\n",
      this.value
    );
  }

}
