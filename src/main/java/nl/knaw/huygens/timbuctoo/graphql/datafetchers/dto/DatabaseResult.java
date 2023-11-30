package nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;

public interface DatabaseResult {
  DataSet getDataSet();
}
