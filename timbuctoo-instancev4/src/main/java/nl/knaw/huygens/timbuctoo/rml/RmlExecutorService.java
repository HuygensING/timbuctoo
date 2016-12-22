package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.rdf.TripleImporter;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.LoggingErrorHandler;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static nl.knaw.huygens.timbuctoo.core.TransactionState.commit;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.COLLECTION_LABEL_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.HAS_COLLECTION_RELATION_NAME;

public class RmlExecutorService {
  public static final Logger LOG = LoggerFactory.getLogger(RmlExecutorService.class);

  private final TransactionEnforcer transactionEnforcer;
  private final String vreName;
  private final TinkerPopGraphManager graphWrapper;
  private final Model model;
  private final RmlMappingDocument rmlMappingDocument;
  private final Vres vres;

  public RmlExecutorService(TransactionEnforcer transactionEnforcer, String vreName, TinkerPopGraphManager graphWrapper,
                            Model model, RmlMappingDocument rmlMappingDocument, Vres vres) {
    this.transactionEnforcer = transactionEnforcer;
    this.vreName = vreName;
    this.graphWrapper = graphWrapper;
    this.model = model;
    this.rmlMappingDocument = rmlMappingDocument;
    this.vres = vres;
  }

  public void execute(Consumer<String> statusUpdate) {
    transactionEnforcer.execute(timbuctooActions -> {
      timbuctooActions.rdfCleanImportSession(vreName, session -> {
        final TripleImporter importer = new TripleImporter(graphWrapper, vreName, session);

        //first save the archetype mappings
        AtomicLong tripleCount = new AtomicLong(0);

        //create the links from the collection entities to the archetypes
        model
          .listStatements(
            null,
            model.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"),
            (String) null
          )
          .forEachRemaining(statement -> {
              importer.importTriple(true, new Triple(
                statement.getSubject().asNode(),
                statement.getPredicate().asNode(),
                statement.getObject().asNode()
              ));
              reportTripleCount(tripleCount, statusUpdate);
            }
          );

        //generate and import rdf
        rmlMappingDocument.execute(new LoggingErrorHandler()).forEach(
          (triple) -> {
            reportTripleCount(tripleCount, statusUpdate);
            importer.importTriple(true, triple);
          });

        reportTripleCount(tripleCount, statusUpdate);

        //Give the collections a proper name
        graphWrapper
          .getGraph()
          .traversal()
          .V()
          .hasLabel(Vre.DATABASE_LABEL)
          .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
          .out(HAS_COLLECTION_RELATION_NAME)
          .forEachRemaining(v -> {
            if (!v.property(COLLECTION_LABEL_PROPERTY_NAME).isPresent()) {
              String typeName = v.value(ENTITY_TYPE_NAME_PROPERTY_NAME);
              v.property(COLLECTION_LABEL_PROPERTY_NAME, typeName.substring(vreName.length()));
            }
          });

        vres.reload();//FIXME naar importSession.close can be done when the Vres are retrieved via TimbuctooActions
        return commit();
      });

      return commit();
    });
  }

  private void reportTripleCount(AtomicLong tripleCount, Consumer<String> statusUpdate) {
    final long curCount = tripleCount.incrementAndGet();
    final String message = String.format("Processed %d triples", curCount);
    statusUpdate.accept(message);
    if (LOG.isDebugEnabled()) {
      LOG.debug(message);
    }
  }
}
