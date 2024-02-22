package nl.knaw.huygens.timbuctoo.graphql.mutations.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.graphql.mutations.Change;
import nl.knaw.huygens.timbuctoo.graphql.mutations.Change.Value;
import nl.knaw.huygens.timbuctoo.util.Graph;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@JsonTypeName("EditMutationChangeLog")
public class EditMutationChangeLog extends ChangeLog {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @JsonProperty
  private final Graph graph;

  @JsonProperty
  private final String subject;

  @JsonProperty
  private final RawEditChangeLog changeLog;

  public EditMutationChangeLog(Graph graph, String subject, Map entity) throws JsonProcessingException {
    this.graph = graph;
    this.subject = subject;

    TreeNode jsonNode = OBJECT_MAPPER.valueToTree(entity);
    this.changeLog = OBJECT_MAPPER.treeToValue(jsonNode, RawEditChangeLog.class);
  }

  private EditMutationChangeLog(Graph graph, String subject, RawEditChangeLog changeLog) {
    this.graph = graph;
    this.subject = subject;
    this.changeLog = changeLog;
  }

  @JsonCreator
  public static EditMutationChangeLog fromJson(@JsonProperty("graph") Graph graph,
                                               @JsonProperty("subject") String subject,
                                               @JsonProperty("changeLog") RawEditChangeLog changeLog) {
    return new EditMutationChangeLog(graph, subject, changeLog);
  }

  @Override
  public Stream<Change> getProvenance(DataSet dataSet, String... subjects) {
    return getProvenanceChanges(dataSet, graph, subjects, dataSet.getCustomProvenance(), changeLog.provenance());
  }

  @Override
  public Stream<Change> getAdditions(DataSet dataSet) {
    Stream<Change> additions =
      changeLog.additions().entrySet().stream()
               .map(entry -> createAdditionsChange(dataSet, entry.getKey(), entry.getValue()));

    Stream<Change> replacementAdditions =
      changeLog.replacements().entrySet().stream()
               .filter(entry -> !hasOldValues(dataSet, entry))
               .filter(entry -> !entry.getValue().isNull() &&
                 !(entry.getValue().isArray() && entry.getValue().isEmpty()))
               .map(entry -> createAdditionsChange(dataSet, entry.getKey(), entry.getValue()));

    return Stream.concat(additions, replacementAdditions);
  }

  @Override
  public Stream<Change> getDeletions(DataSet dataSet) {
    Stream<Change> deletions =
      changeLog.deletions().entrySet().stream()
               .map(entry -> createDeletionsChange(dataSet, entry.getKey(), entry.getValue()));

    Stream<Change> replacementDeletions =
      changeLog.replacements().entrySet().stream()
               .filter(entry -> hasOldValues(dataSet, entry))
               .filter(entry -> entry.getValue().isNull() ||
                 (entry.getValue().isArray() && entry.getValue().isEmpty()))
               .map(entry -> createDeletionsChange(dataSet, entry.getKey(), entry.getValue()));

    return Stream.concat(deletions, replacementDeletions);
  }

  @Override
  public Stream<Change> getReplacements(DataSet dataSet) {
    return changeLog.replacements().entrySet().stream()
                    .filter(entry -> hasOldValues(dataSet, entry))
                    .filter(entry -> !entry.getValue().isNull() &&
                      !(entry.getValue().isArray() && entry.getValue().isEmpty()))
                    .map((entry) -> createReplacementsChange(dataSet, entry.getKey(), entry.getValue()));
  }

  private boolean hasOldValues(DataSet dataSet, Map.Entry<String, JsonNode> entry) {
    try (Stream<CursorQuad> quads = dataSet.getQuadStore().getQuadsInGraph(
      subject,
      dataSet.getTypeNameStore().makeUriForPredicate(entry.getKey()).get().left(),
      Direction.OUT,
      "",
      Optional.of(graph)
    )) {
      return quads.findAny().isPresent();
    }
  }

  private Change createAdditionsChange(DataSet dataSet, String graphQlpred, JsonNode val) {
    String pred = getPredicate(dataSet, graphQlpred);
    List<Value> values = getValues(dataSet, val);

    return new Change(graph, subject, pred, values, Stream.empty());
  }

  private Change createDeletionsChange(DataSet dataSet, String graphQlpred, JsonNode val) {
    List<Value> values = getValues(dataSet, val);

    String pred = getPredicate(dataSet, graphQlpred);
    Stream<Value> oldValues = getOldValues(dataSet, graph, subject, pred)
                                       .filter(value -> values.isEmpty() || values.contains(value));

    return new Change(graph, subject, pred, Lists.newArrayList(), oldValues);
  }

  private Change createReplacementsChange(DataSet dataSet, String graphQlpred, JsonNode val) {
    String pred = getPredicate(dataSet, graphQlpred);
    Stream<Value> oldValues = getOldValues(dataSet, graph, subject, pred);
    List<Value> values = getValues(dataSet, val);

    return new Change(graph, subject, pred, values, oldValues);
  }

  public record RawEditChangeLog(LinkedHashMap<String, ArrayNode> additions,
                                 LinkedHashMap<String, ArrayNode> deletions,
                                 LinkedHashMap<String, JsonNode> replacements,
                                 LinkedHashMap<String, JsonNode> provenance) {
    @JsonCreator
    public RawEditChangeLog(
        @JsonProperty("additions") LinkedHashMap<String, ArrayNode> additions,
        @JsonProperty("deletions") LinkedHashMap<String, ArrayNode> deletions,
        @JsonProperty("replacements") LinkedHashMap<String, JsonNode> replacements,
        @JsonProperty("provenance") LinkedHashMap<String, JsonNode> provenance) {
      this.additions = additions == null ? Maps.newLinkedHashMap() : additions;
      this.deletions = deletions == null ? Maps.newLinkedHashMap() : deletions;
      this.replacements = replacements == null ? Maps.newLinkedHashMap() : replacements;
      this.provenance = provenance == null ? Maps.newLinkedHashMap() : provenance;
    }
  }
}
