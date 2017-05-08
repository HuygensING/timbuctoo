package nl.knaw.huygens.timbuctoo.v5.rdfreader.implementations.jena;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

public class RdfStreamReader implements StreamRDF {
  private final QuadHandler quadHandler;
  private final String fileUri;
  private final long idx;

  public RdfStreamReader(QuadHandler quadHandler, String fileUri) {
    this.quadHandler = quadHandler;
    this.fileUri = fileUri;
    this.idx = 0;
  }

  @Override
  public void start() {
    try {
      quadHandler.start(0);
    } catch (LogProcessingFailedException e) {
      e.printStackTrace();//FIXME! replace with error reporting
    }
  }

  @Override
  public void triple(Triple triple) {
    sendQuad(triple.getSubject(), triple.getPredicate(), triple.getObject(), this.fileUri);
  }

  @Override
  public void quad(Quad quad) {
    sendQuad(quad.getSubject(), quad.getPredicate(), quad.getObject(), quad.getGraph().toString());
  }

  private void sendQuad(Node subjectNode, Node predicateNode, Node objectNode, String graph) {
    String subject = subjectNode.toString();
    String predicate = predicateNode.toString();
    //Use the uri of the current file as the graph name if no graph name is specified
    try {
      if (objectNode.isLiteral()) {
        String literalDataTypeUri = objectNode.getLiteral().getDatatypeURI();
        String value = objectNode.getLiteral().getLexicalForm();
        if (RdfConstants.LANGSTRING.equals(literalDataTypeUri)) {
          quadHandler.onLanguageTaggedString(idx, subject, predicate, value, objectNode.getLiteralLanguage(), graph);
        } else {
          quadHandler.onLiteral(idx, subject, predicate, value, literalDataTypeUri, graph);
        }
      } else {
        quadHandler.onRelation(idx, subject, predicate, objectNode.toString(false), graph);
      }
    } catch (LogProcessingFailedException e) {
      e.printStackTrace();//FIXME! replace with error reporting
    }
  }

  @Override
  public void base(String base) {
  }

  @Override
  public void prefix(String prefix, String iri) {
    try {
      quadHandler.onPrefix(idx, prefix, iri);
    } catch (LogProcessingFailedException e) {
      e.printStackTrace();//FIXME! replace with error reporting
    }
  }

  @Override
  public void finish() {
    try {
      quadHandler.finish();
    } catch (LogProcessingFailedException e) {
      e.printStackTrace();//FIXME! replace with error reporting
    }
  }
}
