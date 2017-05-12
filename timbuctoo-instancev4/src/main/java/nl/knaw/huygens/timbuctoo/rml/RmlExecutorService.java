package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.LoggingErrorHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogStorageFailedException;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

public class RmlExecutorService {
  public static final Logger LOG = LoggerFactory.getLogger(RmlExecutorService.class);

  private final String vreName;
  private final Model model;
  private final RmlMappingDocument rmlMappingDocument;
  private final ImportManager importManager;

  public RmlExecutorService(String vreName, Model model, RmlMappingDocument rmlMappingDocument,
                            ImportManager importManager) {
    this.vreName = vreName;
    this.model = model;
    this.rmlMappingDocument = rmlMappingDocument;
    this.importManager = importManager;
  }

  public void execute() throws LogStorageFailedException, IOException, LogProcessingFailedException {
    importManager.generateQuads(vreName, saver -> {
      //create the links from the collection entities to the archetypes
      StmtIterator statements = model
        .listStatements(
          null,
          model.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"),
          (String) null
        );
      while (statements.hasNext()) {
        Statement statement = statements.next();
        saver.onQuad(
          statement.getSubject().toString(),
          statement.getPredicate().toString(),
          statement.getObject().toString(),
          statement.getObject().isLiteral() ?
            statement.getObject().asLiteral().getDatatype().getURI() :
            (String) null,
          statement.getObject().isLiteral() ?
            statement.getObject().asLiteral().getDatatype().getURI() :
            (String) null,
          "http://somegraph"
        );
      }

      //generate and import rdf
      final Iterator<Triple> iterator;
      try (Stream<Triple> rmlExecution = rmlMappingDocument.execute(new LoggingErrorHandler())) {
        iterator = rmlExecution.iterator();
        while (iterator.hasNext()) {
          Triple triple = iterator.next();
          saver.onQuad(
            triple.getSubject().toString(),
            triple.getPredicate().toString(),
            triple.getObject().toString(false),
            triple.getObject().isLiteral() ?
              triple.getObject().getLiteralDatatypeURI() :
              (String) null,
            triple.getObject().isLiteral() && !triple.getObject().getLiteralLanguage().isEmpty() ?
              triple.getObject().getLiteralLanguage() :
              (String) null,
            "http://somegraph"
          );
        }

      }
    });

  }

}
