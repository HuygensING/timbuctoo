package nl.knaw.huygens.timbuctoo.graphql.mutations.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.graphql.mutations.Change;
import nl.knaw.huygens.timbuctoo.util.Graph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.groupingBy;

@JsonTypeName("DeleteMutationChangeLog")
public class DeleteMutationChangeLog extends ChangeLog {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @JsonProperty
  private final Graph graph;

  @JsonProperty
  private final String subject;

  @JsonProperty
  private final DeleteMutationChangeLog.RawDeleteChangeLog changeLog;

  public DeleteMutationChangeLog(Graph graph, String subject, Map entity) throws JsonProcessingException {
    this.graph = graph;
    this.subject = subject;

    if (entity != null) {
      TreeNode jsonNode = OBJECT_MAPPER.valueToTree(entity);
      this.changeLog = OBJECT_MAPPER.treeToValue(jsonNode, DeleteMutationChangeLog.RawDeleteChangeLog.class);
    } else {
      this.changeLog = new RawDeleteChangeLog(null);
    }
  }

  private DeleteMutationChangeLog(Graph graph, String subject, RawDeleteChangeLog changeLog) {
    this.graph = graph;
    this.subject = subject;
    this.changeLog = (changeLog != null) ? changeLog : new RawDeleteChangeLog(null);
  }

  @JsonCreator
  public static DeleteMutationChangeLog fromJson(@JsonProperty("graph") Graph graph,
                                                 @JsonProperty("subject") String subject,
                                                 @JsonProperty("changeLog") RawDeleteChangeLog changeLog) {
    return new DeleteMutationChangeLog(graph, subject, changeLog);
  }

  @Override
  public Stream<Change> getProvenance(DataSet dataSet, String... subjects) {
    return getProvenanceChanges(dataSet, graph, subjects, dataSet.getCustomProvenance(), changeLog.provenance());
  }

  @Override
  public Stream<Change> getAdditions(DataSet dataSet) {
    return Stream.empty();
  }

  @Override
  public Stream<Change> getDeletions(DataSet dataSet) {
    try (Stream<CursorQuad> quads = dataSet.getQuadStore().getQuadsInGraph(subject, Optional.of(graph))) {
      return quads
        .map(quad -> new Tuple<>(
          quad.getPredicate(),
          new Change.Value(quad.getObject(), quad.getValuetype().orElse(null))
        ))
        .collect(groupingBy(Tuple::left, mapping(Tuple::right, toList())))
        .entrySet().stream()
        .map(predValues ->
          new Change(graph, subject, predValues.getKey(), Lists.newArrayList(), predValues.getValue().stream()));
    }
  }

  @Override
  public Stream<Change> getReplacements(DataSet dataSet) {
    return Stream.empty();
  }

  public record RawDeleteChangeLog(LinkedHashMap<String, JsonNode> provenance) {
    @JsonCreator
    public RawDeleteChangeLog(@JsonProperty("provenance") LinkedHashMap<String, JsonNode> provenance) {
      this.provenance = provenance == null ? Maps.newLinkedHashMap() : provenance;
    }
  }
}
