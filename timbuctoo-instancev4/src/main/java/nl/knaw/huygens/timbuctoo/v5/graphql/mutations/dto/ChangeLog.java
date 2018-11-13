package nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.Change;

import java.util.stream.Stream;

public interface ChangeLog {
  Stream<Change> getAdditions(DataSet dataSet);

  Stream<Change> getDeletions(DataSet dataSet);

  Stream<Change> getReplacements(DataSet dataSet);
}
