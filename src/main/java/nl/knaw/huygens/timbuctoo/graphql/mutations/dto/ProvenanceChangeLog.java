package nl.knaw.huygens.timbuctoo.graphql.mutations.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.graphql.mutations.Change;
import nl.knaw.huygens.timbuctoo.util.Graph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ProvenanceChangeLog extends ChangeLog {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final RawProvenanceChangeLog changeLog;

  public ProvenanceChangeLog(Map entity) throws JsonProcessingException {
    TreeNode jsonNode = OBJECT_MAPPER.valueToTree(entity);
    this.changeLog = OBJECT_MAPPER.treeToValue(jsonNode, RawProvenanceChangeLog.class);
  }

  @Override
  public Stream<Change> getProvenance(DataSet dataSet, String... subjects) {
    return getProvenanceChanges(dataSet, new Graph(null), subjects,
        dataSet.getCustomProvenance(), changeLog.provenance());
  }

  @Override
  public Stream<Change> getAdditions(DataSet dataSet) {
    return Stream.empty();
  }

  @Override
  public Stream<Change> getDeletions(DataSet dataSet) {
    return Stream.empty();
  }

  @Override
  public Stream<Change> getReplacements(DataSet dataSet) {
    return Stream.empty();
  }

  public record RawProvenanceChangeLog(LinkedHashMap<String, JsonNode> provenance) {
    @JsonCreator
    public RawProvenanceChangeLog(@JsonProperty("provenance") LinkedHashMap<String, JsonNode> provenance) {
      this.provenance = provenance == null ? Maps.newLinkedHashMap() : provenance;
    }
  }
}
