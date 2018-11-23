package nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.Change;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "CreateMutationChangeLog", value = CreateMutationChangeLog.class),
    @JsonSubTypes.Type(name = "EditMutationChangeLog", value = EditMutationChangeLog.class),
    @JsonSubTypes.Type(name = "DeleteMutationChangeLog", value = DeleteMutationChangeLog.class)
  })
public abstract class ChangeLog {
  public abstract Stream<Change> getAdditions(DataSet dataSet);

  public abstract Stream<Change> getDeletions(DataSet dataSet);

  public abstract Stream<Change> getReplacements(DataSet dataSet);

  protected String getPredicate(DataSet dataSet, String graphQlpred) {
    TypeNameStore typeNameStore = dataSet.getTypeNameStore();
    return typeNameStore.makeUriForPredicate(graphQlpred).get().getLeft();
  }

  protected Stream<Change.Value> getOldValues(DataSet dataSet, String subject, String pred) {
    return dataSet.getQuadStore()
                  .getQuads(subject, pred, Direction.OUT, "")
                  .map(quad -> new Change.Value(quad.getObject(), quad.getValuetype().orElse(null)));
  }

  protected List<Change.Value> getValues(DataSet dataSet, JsonNode val) {
    List<Change.Value> values;
    if (val.isNull()) {
      values = Lists.newArrayList();
    } else if (val.isArray()) {
      values =
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(val.iterator(), Spliterator.ORDERED), false)
                     .map(value -> {
                       String rawValue = value.get("value").asText();
                       String valueType = dataSet.getTypeNameStore().makeUri(value.get("type").asText());
                       return new Change.Value(rawValue, valueType);
                     }).collect(Collectors.toList());
    } else if (val.has("value") && val.has("type")) {
      String value = val.get("value").asText();
      String valueType = dataSet.getTypeNameStore().makeUri(val.get("type").asText());
      values = Lists.newArrayList(new Change.Value(value, valueType));
    } else {
      throw new IllegalArgumentException("'" + val + "' is not a valid value");
    }

    return values;
  }
}
