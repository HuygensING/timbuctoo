package nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.Change;

import java.util.stream.Stream;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "EditMutationChangeLog", value = EditMutationChangeLog.class)
  })
public interface ChangeLog {
  Stream<Change> getAdditions(DataSet dataSet);

  Stream<Change> getDeletions(DataSet dataSet);

  Stream<Change> getReplacements(DataSet dataSet);
}
