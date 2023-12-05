package nl.knaw.huygens.timbuctoo.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectReference;

public interface RelatedDataFetcher<T extends DatabaseResult> {
  PaginatedList<T> getList(SubjectReference source, PaginationArguments arguments, DataSet dataSet);

  T getItem(SubjectReference source, DataSet dataSet);
}
