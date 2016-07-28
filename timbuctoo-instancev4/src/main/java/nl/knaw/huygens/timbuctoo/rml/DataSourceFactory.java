package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlLogicalSource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.rmlsources.TimbuctooRawCollectionSource;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;

import java.util.function.Function;

public class DataSourceFactory implements Function<RmlLogicalSource, DataSource> {
  private final GraphWrapper graphWrapper;

  public DataSourceFactory(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  @Override
  public DataSource apply(RmlLogicalSource rmlLogicalSource) {
    if (rmlLogicalSource.getSource() instanceof TimbuctooRawCollectionSource) {
      return new BulkUploadedDataSource((TimbuctooRawCollectionSource) rmlLogicalSource.getSource(), graphWrapper);
    } else {
      return null;
    }
  }
}
