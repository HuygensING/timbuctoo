package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.VreBuilder;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;

import java.util.Optional;

public class ImportPreparer {
  private final GraphWrapper graphWrapper;

  public ImportPreparer(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  public void setUpAdminVre() {
    Vre vre = VreBuilder.vre("Admin", "").withCollection("concepts").build();
    vre.save(graphWrapper.getGraph(), Optional.empty());
  }

  public void setupVre(String vreName) {
    Vre vre = VreBuilder.vre(vreName, vreName).build();
    vre.save(graphWrapper.getGraph(), Optional.empty());
  }
}
