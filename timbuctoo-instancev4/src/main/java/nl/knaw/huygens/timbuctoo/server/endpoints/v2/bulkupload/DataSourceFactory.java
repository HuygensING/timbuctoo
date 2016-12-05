package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rdfshim.RdfResource;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class DataSourceFactory implements Function<RdfResource, Optional<DataSource>> {
  private final GraphWrapper graphWrapper;
  private static final String NS_RML = "http://semweb.mmlab.be/ns/rml#";

  public DataSourceFactory(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  @Override
  public Optional<DataSource> apply(RdfResource rdfResource) {
    for (RdfResource resource : rdfResource.out(NS_RML + "source")) {
      Set<RdfResource> rawCollection = resource.out("http://timbuctoo.huygens.knaw.nl/mapping#rawCollection");
      Set<RdfResource> vreName = resource.out("http://timbuctoo.huygens.knaw.nl/mapping#vreName");

      if (rawCollection.size() == 1 && vreName.size() == 1) {
        return Optional.of(new BulkUploadedDataSource(
          vreName.iterator().next().asLiteral().get().getValue(),
          rawCollection.iterator().next().asLiteral().get().getValue(),
          graphWrapper
        ));
      }
    }
    return Optional.empty();
  }
}
