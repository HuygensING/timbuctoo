package nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.Change;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.Change.Value;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;

public class EditMutationChangeLog implements ChangeLog {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @JsonProperty
  private final String subject;

  @JsonProperty
  private final RawEditChangeLog changeLog;

  public EditMutationChangeLog(String subject, Map entity) throws JsonProcessingException {
    this.subject = subject;

    TreeNode jsonNode = OBJECT_MAPPER.valueToTree(entity);
    this.changeLog = OBJECT_MAPPER.treeToValue(jsonNode, RawEditChangeLog.class);
  }

  private EditMutationChangeLog(String subject, RawEditChangeLog changeLog) {
    this.subject = subject;
    this.changeLog = changeLog;
  }

  @JsonCreator
  public static EditMutationChangeLog fromJson(@JsonProperty("subject") String subject,
                                               @JsonProperty("changeLog") RawEditChangeLog changeLog) {
    return new EditMutationChangeLog(subject, changeLog);
  }

  @Override
  public Stream<Change> getAdditions(DataSet dataSet) {
    Stream<Change> additions =
      changeLog.getAdditions().entrySet().stream()
               .map(entry -> createAdditionsChange(dataSet, entry.getKey(), entry.getValue()));

    Stream<Change> replacementAdditions =
      changeLog.getReplacements().entrySet().stream()
               .filter(entry -> !hasOldValues(dataSet, entry))
               .filter(entry -> !entry.getValue().isNull() &&
                 !(entry.getValue().isArray() && entry.getValue().size() == 0))
               .map(entry -> createAdditionsChange(dataSet, entry.getKey(), entry.getValue()));

    return Stream.concat(additions, replacementAdditions);
  }

  @Override
  public Stream<Change> getDeletions(DataSet dataSet) {
    Stream<Change> deletions =
      changeLog.getDeletions().entrySet().stream()
               .map(entry -> createDeletionsChange(dataSet, entry.getKey(), entry.getValue()));

    Stream<Change> replacementDeletions =
      changeLog.getReplacements().entrySet().stream()
               .filter(entry -> hasOldValues(dataSet, entry))
               .filter(entry -> entry.getValue().isNull() ||
                 (entry.getValue().isArray() && entry.getValue().size() == 0))
               .map(entry -> createDeletionsChange(dataSet, entry.getKey(), entry.getValue()));

    return Stream.concat(deletions, replacementDeletions);
  }

  @Override
  public Stream<Change> getReplacements(DataSet dataSet) {
    return changeLog.getReplacements().entrySet().stream()
                    .filter(entry -> hasOldValues(dataSet, entry))
                    .filter(entry -> !entry.getValue().isNull() &&
                      !(entry.getValue().isArray() && entry.getValue().size() == 0))
                    .map((entry) -> createReplacementsChange(dataSet, entry.getKey(), entry.getValue()));
  }

  private boolean hasOldValues(DataSet dataSet, Map.Entry<String, JsonNode> entry) {
    try (Stream<CursorQuad> quads = dataSet.getQuadStore().getQuads(
      subject,
      dataSet.getTypeNameStore().makeUriForPredicate(entry.getKey()).get().getLeft(),
      Direction.OUT,
      ""
    )) {
      return quads.findAny().isPresent();
    }
  }

  private Change createAdditionsChange(DataSet dataSet, String graphQlpred, JsonNode val) {
    String pred = getPredicate(dataSet, graphQlpred);
    // FIXME make it work with reference types
    List<Value> values = getValues(dataSet, val);

    return new Change(subject, pred, values, Stream.empty());
  }

  private Change createDeletionsChange(DataSet dataSet, String graphQlpred, JsonNode val) {
    List<Value> values = getValues(dataSet, val);

    String pred = getPredicate(dataSet, graphQlpred);
    // FIXME make it work with reference types
    Stream<Value> oldValues = getOldValues(dataSet, pred)
      .filter(value -> values.isEmpty() || values.contains(value));

    return new Change(subject, pred, Lists.newArrayList(), oldValues);
  }

  private Change createReplacementsChange(DataSet dataSet, String graphQlpred, JsonNode val) {
    String pred = getPredicate(dataSet, graphQlpred);
    // FIXME make it work with reference types
    Stream<Value> oldValues = getOldValues(dataSet, pred);
    List<Value> values = getValues(dataSet, val);

    return new Change(subject, pred, values, oldValues);
  }

  private Stream<Value> getOldValues(DataSet dataSet, String pred) {
    return dataSet.getQuadStore().getQuads(subject, pred, Direction.OUT, "")
                  .map(quad -> new Value(quad.getObject(), quad.getValuetype().orElse(STRING)));
  }

  private static List<Value> getValues(DataSet dataSet, JsonNode val) {
    List<Value> values;
    if (val.isNull()) {
      values = Lists.newArrayList();
    } else if (val.isArray()) {
      values =
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(val.iterator(), Spliterator.ORDERED), false)
                     .map(value -> {
                       String rawValue = value.get("value").asText();
                       String valueType = dataSet.getTypeNameStore().makeUri(value.get("type").asText());
                       return new Value(rawValue, valueType);
                     }).collect(Collectors.toList());
    } else if (val.has("value") && val.has("type")) {
      String value = val.get("value").asText();
      String valueType = dataSet.getTypeNameStore().makeUri(val.get("type").asText());
      values = Lists.newArrayList(new Value(value, valueType));
    } else {
      throw new IllegalArgumentException("'" + val + "' is not a valid value");
    }

    return values;
  }

  private static String getPredicate(DataSet dataSet, String graphQlpred) {
    TypeNameStore typeNameStore = dataSet.getTypeNameStore();
    return typeNameStore.makeUriForPredicate(graphQlpred).get().getLeft();
  }

  public static class RawEditChangeLog {
    private LinkedHashMap<String, ArrayNode> additions;
    private LinkedHashMap<String, ArrayNode> deletions;
    private LinkedHashMap<String, JsonNode> replacements;

    @JsonCreator
    public RawEditChangeLog(
      @JsonProperty("additions") LinkedHashMap<String, ArrayNode> additions,
      @JsonProperty("deletions") LinkedHashMap<String, ArrayNode> deletions,
      @JsonProperty("replacements") LinkedHashMap<String, JsonNode> replacements) {
      this.additions = additions == null ? Maps.newLinkedHashMap() : additions;
      this.deletions = deletions == null ? Maps.newLinkedHashMap() : deletions;
      this.replacements = replacements == null ? Maps.newLinkedHashMap() : replacements;
    }


    public LinkedHashMap<String, ArrayNode> getAdditions() {
      return additions;
    }

    public LinkedHashMap<String, ArrayNode> getDeletions() {
      return deletions;
    }

    public LinkedHashMap<String, JsonNode> getReplacements() {
      return replacements;
    }
  }
}
