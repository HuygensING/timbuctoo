package nl.knaw.huygens.timbuctoo.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectReference;

public interface CollectionFetcher {
  PaginatedList<SubjectReference> getList(PaginationArguments arguments, DataSet dataSet);
}
