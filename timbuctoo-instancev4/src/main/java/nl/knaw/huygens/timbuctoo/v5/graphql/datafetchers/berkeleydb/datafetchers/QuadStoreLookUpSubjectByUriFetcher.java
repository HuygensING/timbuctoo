package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.LookUpSubjectByUriFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.util.Graph;

import java.util.Optional;

public class QuadStoreLookUpSubjectByUriFetcher implements LookUpSubjectByUriFetcher {
  public QuadStoreLookUpSubjectByUriFetcher() {
  }

  @Override
  public SubjectReference getItem(String uri, DataSet dataSet) {
    return new LazyTypeSubjectReference(uri, Optional.empty(), dataSet);
  }

  @Override
  public SubjectReference getItemInGraph(String uri, Optional<Graph> graph, DataSet dataSet) {
    return new LazyTypeSubjectReference(uri, graph, dataSet);
  }
}
