package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class EditMutationChangeLog {
  private GraphQlToRdfPatch.ChangeLog changeLog;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public EditMutationChangeLog(Map entity) throws Exception {
    TreeNode jsonNode = OBJECT_MAPPER.valueToTree(entity);
    changeLog = OBJECT_MAPPER.treeToValue(jsonNode, GraphQlToRdfPatch.ChangeLog.class);
  }

  public Stream<Change> getAdditions(String subjectUri, DataSet dataSet) {
    TypeNameStore typeNameStore = dataSet.getTypeNameStore();

    List<Change> changes = new ArrayList<>();

    for (Map.Entry<String, ArrayNode> addition : changeLog.getAdditions().entrySet()) {
      String predicate = typeNameStore.makeUriForPredicate(addition.getKey()).get().getLeft();

      for (JsonNode propertyInput : addition.getValue()) {
        String value = propertyInput.get("value").asText();
        String valueType = typeNameStore.makeUri(propertyInput.get("type").asText());

        changes.add(new Change(subjectUri, predicate, value, valueType, null, null));
      }
    }

    for (Map.Entry<String, JsonNode> replacements : changeLog.getReplacements().entrySet()) {
      String predicate = typeNameStore.makeUriForPredicate(replacements.getKey()).get().getLeft();

      try (Stream<CursorQuad> quads = dataSet.getQuadStore().getQuads(subjectUri, predicate, Direction.OUT, "")) {
        if (quads.findFirst().isPresent()) {
          continue;
        }
      }

      if (replacements.getValue().isArray()) {
        for (JsonNode propertyInput : replacements.getValue()) {
          String value = propertyInput.get("value").asText();
          String valueType = typeNameStore.makeUri(propertyInput.get("type").asText());

          changes.add(new Change(subjectUri, predicate, value, valueType, null, null));
        }
      } else {
        String value = replacements.getValue().get("value").asText();
        String valueType = typeNameStore.makeUri(replacements.getValue().get("type").asText());

        changes.add(new Change(subjectUri, predicate, value, valueType, null, null));
      }
    }

    return changes.stream();
  }
}
