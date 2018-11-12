package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.Change.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;

class EditMutationChangeLog {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private GraphQlToRdfPatch.ChangeLog changeLog;

  public EditMutationChangeLog(Map entity) throws Exception {
    TreeNode jsonNode = OBJECT_MAPPER.valueToTree(entity);
    changeLog = OBJECT_MAPPER.treeToValue(jsonNode, GraphQlToRdfPatch.ChangeLog.class);
  }

  public Stream<Change> getAdditions(String subjectUri, DataSet dataSet) {
    TypeNameStore typeNameStore = dataSet.getTypeNameStore();

    List<Change> changes = new ArrayList<>();

    for (Map.Entry<String, ArrayNode> addition : changeLog.getAdditions().entrySet()) {
      String predicate = typeNameStore.makeUriForPredicate(addition.getKey()).get().getLeft(); //TODO add better fail

      List<Value> values = Lists.newArrayList();
      for (JsonNode propertyInput : addition.getValue()) {
        String value = propertyInput.get("value").asText();
        String valueType = typeNameStore.makeUri(propertyInput.get("type").asText());
        values.add(new Value(value, valueType));
      }
      changes.add(new Change(subjectUri, predicate, values, null));
    }

    for (Map.Entry<String, JsonNode> replacements : changeLog.getReplacements().entrySet()) {
      String predicate = typeNameStore.makeUriForPredicate(replacements.getKey()).get().getLeft();

      try (Stream<CursorQuad> quads = dataSet.getQuadStore().getQuads(subjectUri, predicate, Direction.OUT, "")) {
        if (quads.findFirst().isPresent()) {
          continue;
        }
      }

      if (replacements.getValue().isArray()) {
        List<Value> values = Lists.newArrayList();
        for (JsonNode propertyInput : replacements.getValue()) {
          String value = propertyInput.get("value").asText();
          String valueType = typeNameStore.makeUri(propertyInput.get("type").asText());
          values.add(new Value(value, valueType));
        }
        changes.add(new Change(subjectUri, predicate, values, null));
      } else {
        JsonNode propertyInput = replacements.getValue();
        String value = propertyInput.get("value").asText();
        String valueType = typeNameStore.makeUri(propertyInput.get("type").asText());

        changes.add(new Change(subjectUri, predicate, Lists.newArrayList(new Value(value, valueType)), null));
      }
    }

    return changes.stream();
  }

  public Stream<Change> getReplacements(String subject, DataSet dataSet) {
    return changeLog.getReplacements().entrySet().stream()
                    .filter(entry -> {
                      return dataSet.getQuadStore().getQuads(
                        subject,
                        dataSet.getTypeNameStore().makeUriForPredicate(entry.getKey()).get().getLeft(),
                        Direction.OUT,
                        ""
                      ).findAny().isPresent();
                    }).map((entry) -> createChange(subject, dataSet, entry.getKey(), entry.getValue()));
  }

  private Change createChange(String subject, DataSet dataSet, String graphQlpred, JsonNode val) {
    TypeNameStore typeNameStore = dataSet.getTypeNameStore();
    String pred = typeNameStore.makeUriForPredicate(graphQlpred).get().getLeft();
    // FIXME make it work with reference types
    Stream<Value> oldValues = dataSet.getQuadStore().getQuads(subject, pred, Direction.OUT, "")
                                     .map(quad -> new Value(quad.getObject(), quad.getValuetype().orElse(STRING)));

    List<Value> values;
    if (val.isArray()) {
      values =
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(val.iterator(), Spliterator.ORDERED), false)
                     .map(value -> {
                       String rawValue = value.get("value").asText();
                       String valueType = typeNameStore.makeUri(value.get("type").asText());
                       return new Value(rawValue, valueType);
                     }).collect(Collectors.toList());
    } else if (val.has("value") && val.has("type")) {
      String value = val.get("value").asText();
      String valueType = typeNameStore.makeUri(val.get("type").asText());
      values = Lists.newArrayList(new Value(value, valueType));
    } else {
      throw new IllegalArgumentException("'" + val + "' is not a valid value");
    }

    return new Change(subject, pred, values, oldValues);
  }
}
