package nl.knaw.huygens.timbuctoo.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectGraphReference;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

public class GraphsFetcher implements DataFetcher<List<String>> {
  @Override
  public List<String> get(DataFetchingEnvironment env) {
    if (env.getSource() instanceof SubjectGraphReference) {
      SubjectGraphReference source = env.getSource();
      DataSet dataSet = source.getDataSet();

      try (Stream<CursorQuad> stream =
               dataSet.getQuadStore().getQuadsInGraph(source.getSubjectUri(), source.getGraph())) {
        return new ArrayList<>(stream.collect(groupingBy(q -> q.getGraph().orElse(""))).keySet());
      }
    }

    return null;
  }
}
