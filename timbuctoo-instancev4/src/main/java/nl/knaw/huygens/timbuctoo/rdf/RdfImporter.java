package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
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
  private final TripleImporter tripleImporter;
  private final ImportPreparer importPreparer;
  private Vres vres;

  public RdfImporter(GraphWrapper graphWrapper, String vreName, Vres vres) {
    this(graphWrapper, vreName, vres, new TripleImporter(graphWrapper, vreName), new ImportPreparer(graphWrapper));
  }

  RdfImporter(GraphWrapper graphWrapper, String vreName, Vres vres, TripleImporter tripleImporter,
              ImportPreparer importPreparer) {
    this.graphWrapper = graphWrapper;
    this.vreName = vreName;
    this.tripleImporter = tripleImporter;
    this.importPreparer = importPreparer;
    this.vres = vres;
  }

  public void importRdf(InputStream triples, Lang lang) {
    final Stopwatch stopwatch = Stopwatch.createStarted();
    final StreamRDF tripleStreamReader = new TripleStreamReader();

    RDFDataMgr.parse(tripleStreamReader, new TypedInputStream(triples), lang);

    LOG.info("Import took {}", stopwatch.stop());

  }

  private void complete() {
    graphWrapper.getGraph().tx().commit();
    vres.reload();
  }

  private void prepare() {
    importPreparer.setupVre(vreName);
    importPreparer.setUpAdminVre();
    LOG.info("Starting import...");
  }


  private final class TripleStreamReader implements StreamRDF {
    private final List<Triple> batch = Lists.newArrayList();

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
        batch.forEach(tripleImporter::importTriple);
        graphWrapper.getGraph().tx().commit();
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
