package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.util.Graph;

import java.util.Optional;

public interface LookUpSubjectByUriFetcher {
  SubjectReference getItem(String uri, DataSet dataSet);

  SubjectReference getItemInGraph(String uri, Optional<Graph> graph, DataSet dataSet);
}
