package nl.knaw.huygens.timbuctoo.v5.rdfreader.implementations.jena;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.ThroughputLogger;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

public class RdfStreamReader implements StreamRDF {
  private final QuadHandler quadHandler;
  private final String fileUri;
  private ThroughputLogger logger;

  public RdfStreamReader(QuadHandler quadHandler, String fileUri) {
    this.quadHandler = quadHandler;
    this.fileUri = fileUri;
  }

  @Override
  public void start() {
    this.logger = new ThroughputLogger(10);
    try {
      quadHandler.start();
    } catch (LogProcessingFailedException e) {
      e.printStackTrace();//FIXME! replace with error reporting
    }
  }

  @Override
  public void triple(Triple triple) {
    String subject = triple.getSubject().toString();
    String predicate = triple.getPredicate().toString();
    String object = triple.getObject().toString(false);
    String literalDataTypeUri = triple.getObject().isLiteral() ? triple.getObject().getLiteralDatatypeURI() : null;
    //Use the uri of the current file as the graph name if no graph name is specified
    sendQuad(subject, predicate, object, literalDataTypeUri, this.fileUri);
  }

  @Override
  public void quad(Quad quad) {
    String subject = quad.getSubject().toString();
    String predicate = quad.getPredicate().toString();
    String object = quad.getObject().toString(false);
    String literalDataTypeUri = quad.getObject().isLiteral() ? quad.getObject().getLiteralDatatypeURI() : null;
    String graph = quad.getGraph().toString();
    sendQuad(subject, predicate, object, literalDataTypeUri, graph);
  }

  private void sendQuad(String subject, String predicate, String object, String literalDataTypeUri, String graph) {
    logger.tripleProcessed();
    try {
      quadHandler.onQuad(subject, predicate, object, literalDataTypeUri, graph);
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
      quadHandler.onPrefix(prefix, iri);
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
