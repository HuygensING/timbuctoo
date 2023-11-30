package nl.knaw.huygens.timbuctoo.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.util.Graph;

import java.util.Optional;

public interface LookUpSubjectByUriFetcher {
  SubjectReference getItem(String uri, DataSet dataSet);

  SubjectReference getItemInGraph(String uri, Optional<Graph> graph, DataSet dataSet);
}
