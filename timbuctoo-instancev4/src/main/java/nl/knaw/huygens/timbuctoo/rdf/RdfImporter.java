package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.VreBuilder;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;

import java.util.Optional;

public class RdfImporter {
  private final GraphWrapper graphWrapper;
  private final String vreName;
  private final TripleImporter tripleImporter;

  public RdfImporter(GraphWrapper graphWrapper, String vreName) {
    this(graphWrapper, vreName, new TripleImporter(graphWrapper, vreName));
  }

  RdfImporter(GraphWrapper graphWrapper, String vreName, TripleImporter tripleImporter) {
    this.graphWrapper = graphWrapper;
    this.vreName = vreName;
    this.tripleImporter = tripleImporter;
  }

  public void importRdf(Model model) {
    Vre vre = createVre();
    vre.save(graphWrapper.getGraph(), Optional.empty());

    model.getGraph().find(Triple.ANY).forEachRemaining(tripleImporter::importTriple);
  }

  Vre createVre() {
    return VreBuilder.vre(vreName, vreName).build();
  }
}
