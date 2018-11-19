package nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.Change;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.Change.Value;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@JsonTypeName("CreateMutationChangeLog")
public class CreateMutationChangeLog extends ChangeLog {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @JsonProperty
  private final String subject;

  @JsonProperty
  private final RawCreateChangeLog changeLog;

  public CreateMutationChangeLog(String subject, Map entity) throws JsonProcessingException {
    this.subject = subject;

    TreeNode jsonNode = OBJECT_MAPPER.valueToTree(entity);
    this.changeLog = OBJECT_MAPPER.treeToValue(jsonNode, RawCreateChangeLog.class);
  }

  private CreateMutationChangeLog(String subject, RawCreateChangeLog changeLog) {
    this.subject = subject;
    this.changeLog = changeLog;
  }

  @JsonCreator
  public static CreateMutationChangeLog fromJson(@JsonProperty("subject") String subject,
                                                 @JsonProperty("changeLog") RawCreateChangeLog changeLog) {
    return new CreateMutationChangeLog(subject, changeLog);
  }

  @Override
  public Stream<Change> getAdditions(DataSet dataSet) {
    return changeLog.getCreations().entrySet().stream()
                    .filter(entry -> !entry.getValue().isNull() &&
                      !(entry.getValue().isArray() && entry.getValue().size() == 0))
                    .map(entry -> createChange(dataSet, entry.getKey(), entry.getValue()));
  }

  @Override
  public Stream<Change> getDeletions(DataSet dataSet) {
    return Stream.empty();
  }

  @Override
  public Stream<Change> getReplacements(DataSet dataSet) {
    return Stream.empty();
  }

  private Change createChange(DataSet dataSet, String graphQlpred, JsonNode val) {
    String pred = getPredicate(dataSet, graphQlpred);
    // FIXME make it work with reference types
    List<Value> values = getValues(dataSet, val);

    return new Change(subject, pred, values, Stream.empty());
  }

  public static class RawCreateChangeLog {
    private LinkedHashMap<String, JsonNode> creations;

    @JsonCreator
    public RawCreateChangeLog(@JsonProperty("creations") LinkedHashMap<String, JsonNode> creations) {
      this.creations = creations == null ? Maps.newLinkedHashMap() : creations;
    }

    public LinkedHashMap<String, JsonNode> getCreations() {
      return creations;
    }
  }
}
