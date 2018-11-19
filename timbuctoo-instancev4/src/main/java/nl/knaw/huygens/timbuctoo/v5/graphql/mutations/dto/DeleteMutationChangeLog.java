package nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.Change;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.groupingBy;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;

@JsonTypeName("DeleteMutationChangeLog")
public class DeleteMutationChangeLog extends ChangeLog {
  @JsonProperty
  private final String subject;

  public DeleteMutationChangeLog(String subject) {
    this.subject = subject;
  }

  @JsonCreator
  public static DeleteMutationChangeLog fromJson(@JsonProperty("subject") String subject) {
    return new DeleteMutationChangeLog(subject);
  }

  @Override
  public Stream<Change> getAdditions(DataSet dataSet) {
    return Stream.empty();
  }

  @Override
  public Stream<Change> getDeletions(DataSet dataSet) {
    return dataSet.getQuadStore()
                  .getQuads(subject)
                  .map(quad -> new Tuple<>(
                    quad.getPredicate(),
                    new Change.Value(quad.getObject(), quad.getValuetype().orElse(STRING))
                  ))
                  .collect(groupingBy(Tuple::getLeft, mapping(Tuple::getRight, toList())))
                  .entrySet().stream()
                  .map(predValues ->
                    new Change(subject, predValues.getKey(), Lists.newArrayList(), predValues.getValue().stream()));
  }

  @Override
  public Stream<Change> getReplacements(DataSet dataSet) {
    return Stream.empty();
  }
}
