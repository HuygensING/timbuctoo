package nl.knaw.huygens.timbuctoo.server;

import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.rdf.TripleImporter;
import nl.knaw.huygens.timbuctoo.rml.dto.QuadPart;
import nl.knaw.huygens.timbuctoo.rml.dto.RdfBlankNode;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.LoggingErrorHandler;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static nl.knaw.huygens.timbuctoo.core.TransactionState.commit;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.COLLECTION_LABEL_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection.ENTITY_TYPE_NAME_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.HAS_COLLECTION_RELATION_NAME;
import static org.apache.jena.graph.NodeFactory.createLiteral;

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
      timbuctooActions.setVrePublishState(vreName, Vre.PublishState.MAPPING_EXECUTION);

      timbuctooActions.rdfCleanImportSession(vreName, session -> {
        final TripleImporter importer = new TripleImporter(graphWrapper, vreName, session);

        //first save the archetype mappings
        AtomicLong tripleCount = new AtomicLong(0);
        AtomicLong curtime = new AtomicLong(Clock.systemUTC().millis());

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
              reportTripleCount(tripleCount, curtime, statusUpdate);
            }
          );

        //generate and import rdf
        rmlMappingDocument.execute(new LoggingErrorHandler()).forEach(
          (quad) -> {
            reportTripleCount(tripleCount, curtime, statusUpdate);
            Node object;
            final QuadPart sourceObject = quad.getObject();
            if (sourceObject.getLiteralLanguage().isPresent()) {
              object = createLiteral(sourceObject.getContent(), sourceObject.getLiteralLanguage().get());
            } else if (sourceObject.getLiteralType().isPresent()) {
              object = createLiteral(sourceObject.getContent(), new BaseDatatype(sourceObject.getLiteralType().get()));
            } else if (sourceObject instanceof RdfBlankNode) {
              object = NodeFactory.createBlankNode(sourceObject.getContent());
            } else {
              object = NodeFactory.createURI(sourceObject.getContent());
            }
            importer.importTriple(true, new Triple(
              quad.getSubject() instanceof RdfBlankNode ?
                NodeFactory.createBlankNode(quad.getSubject().getContent()) :
                NodeFactory.createURI(quad.getSubject().getContent()),
              NodeFactory.createURI(quad.getPredicate().getContent()),
              object
            ));
          });

        reportTripleCount(tripleCount, curtime, statusUpdate);

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

        return commit();
      });
      return commit();
    });
    vres.reload();//FIXME naar importSession.close can be done when the Vres are retrieved via TimbuctooActions
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
