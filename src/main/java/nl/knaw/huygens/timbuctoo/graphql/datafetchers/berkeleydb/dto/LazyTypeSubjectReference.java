package nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.dto;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectGraphReference;
import nl.knaw.huygens.timbuctoo.util.Graph;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.RDF_TYPE;

public class LazyTypeSubjectReference implements SubjectGraphReference {
  private final String subjectUri;
  private final Optional<Graph> graph;
  private final DataSet dataSet;
  private Set<String> types;

  public LazyTypeSubjectReference(String subjectUri, Optional<Graph> graph, DataSet dataSet) {
    this.subjectUri = subjectUri;
    this.graph = graph;
    this.dataSet = dataSet;
  }

  @Override
  public String getSubjectUri() {
    return subjectUri;
  }

  @Override
  public Optional<Graph> getGraph() {
    return graph;
  }

  @Override
  public Set<String> getTypes() {
    if (types == null) {
      try (Stream<CursorQuad> quads = dataSet
          .getQuadStore().getQuadsInGraph(subjectUri, RDF_TYPE, Direction.OUT, "", graph)) {
        types = quads
          .map(CursorQuad::getObject)
          .collect(toSet());
      }
    }
    return types;
  }

  @Override
  public DataSet getDataSet() {
    return dataSet;
  }
}
