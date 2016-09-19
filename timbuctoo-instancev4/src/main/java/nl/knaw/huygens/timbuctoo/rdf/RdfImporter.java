package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.database.DataAccess;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.TripleProcessorImpl;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

public class RdfImporter {
  public static final Logger LOG = LoggerFactory.getLogger(RdfImporter.class);
  private final GraphWrapper graphWrapper;
  private final String vreName;
  private final DataAccess dataAccess;
  private final TripleProcessorImpl processor;
  private Vres vres;

  public RdfImporter(GraphWrapper graphWrapper, String vreName, Vres vres, DataAccess dataAccess) {
    this(graphWrapper, vreName, vres, dataAccess, new TripleProcessorImpl(new Database(graphWrapper)));
  }

  RdfImporter(GraphWrapper graphWrapper, String vreName, Vres vres, DataAccess dataAccess,
              TripleProcessorImpl tripleImporter) {
    this.graphWrapper = graphWrapper;
    this.vreName = vreName;
    this.processor = tripleImporter;
    this.dataAccess = dataAccess;
    this.vres = vres;
  }

  public void importRdf(InputStream rdf, Lang lang) {
    final Stopwatch stopwatch = Stopwatch.createStarted();
    final StreamRDF rdfStreamReader = new RdfStreamReader();

    RDFDataMgr.parse(rdfStreamReader, new TypedInputStream(rdf), lang);

    LOG.info("Import took {}", stopwatch.stop());

  }

  private void complete() {
    graphWrapper.getGraph().tx().commit();
    vres.reload();
  }

  private void prepare() {
    dataAccess.execute(db -> {
      db.ensureVreExists(vreName);
    });
    LOG.info("Starting import...");
  }

  private final class RdfStreamReader implements StreamRDF {
    private final List<Triple> batch = Lists.newArrayList();

    private long count = 0;
    private Stopwatch stopwatch = Stopwatch.createStarted() ;

    @Override
    public void start() {
      prepare();
    }

    @Override
    public void triple(Triple triple) {
      batch.add(triple);
      flushBatch(1000);
    }

    @Override
    public void quad(Quad quad) {
      batch.add(quad.asTriple());
      flushBatch(1000);
    }

    private void flushBatch(int size) {
      if (size < 0 || batch.size() >= size) {
        batch.forEach((triple) -> processor.process(vreName, true, triple));
        graphWrapper.getGraph().tx().commit();
        count += batch.size();
        LOG.debug("Currently loaded {} triples", count);
        LOG.debug("Which produced {} entities", graphWrapper
          .getGraph().traversal().V()
          .hasLabel(Vre.DATABASE_LABEL)
          .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
          .out(Vre.HAS_COLLECTION_RELATION_NAME)
          .out(Collection.HAS_ENTITY_NODE_RELATION_NAME)
          .outE(Collection.HAS_ENTITY_RELATION_NAME)
          .count()
          .next());
        LOG.debug("And this batch took {}", stopwatch.stop());
        stopwatch.reset().start();
        batch.clear();
      }
    }

    @Override
    public void base(String base) {
    }

    @Override
    public void prefix(String prefix, String iri) {
    }

    @Override
    public void finish() {
      flushBatch(-1);
      complete();
    }
  }
}
