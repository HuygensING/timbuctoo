package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.LoggingErrorHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadSaver;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.RdfCreator;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogStorageFailedException;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Clock;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.COLLECTION_LABEL_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.HAS_COLLECTION_RELATION_NAME;

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

  public void execute(Consumer<String> statusUpdate)
    throws LogStorageFailedException, IOException, LogProcessingFailedException {
    // timbuctooActions.setVrePublishState(vreName, Vre.PublishState.MAPPING_EXECUTION);
    importManager.generateQuads(vreName, saver -> {
      AtomicLong tripleCount = new AtomicLong(0);
      AtomicLong curtime = new AtomicLong(Clock.systemUTC().millis());

      //create the links from the collection entities to the archetypes
      StmtIterator statements = model
        .listStatements(
          null,
          model.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"),
          (String) null
        );
      while (statements.hasNext()) {
        Statement statement = statements.next();
        saver.onTriple(
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
        reportTripleCount(tripleCount, curtime, statusUpdate);
      }

      //generate and import rdf
      Iterator<Triple> iterator = rmlMappingDocument.execute(new LoggingErrorHandler()).iterator();

      while (iterator.hasNext()) {
        Triple triple = iterator.next();
        reportTripleCount(tripleCount, curtime, statusUpdate);
        saver.onTriple(
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
        reportTripleCount(tripleCount, curtime, statusUpdate);
      }

    });



  }

  private void reportTripleCount(AtomicLong tripleCount, AtomicLong lastLogTime, Consumer<String> statusUpdate) {
    final long curCount = tripleCount.incrementAndGet();
    long curTime = Clock.systemUTC().millis();
    if ((curTime - lastLogTime.get()) > 100) {
      statusUpdate.accept(String.format("Processed %d triples", curCount));
      lastLogTime.set(curTime);
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("Processed %d triples", curCount));
    }
  }
}
