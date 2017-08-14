package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;

public interface RelatedDataFetcher<T extends DatabaseResult> {
  PaginatedList<T> getList(SubjectReference source, PaginationArguments arguments);

  T getItem(SubjectReference source);
}
