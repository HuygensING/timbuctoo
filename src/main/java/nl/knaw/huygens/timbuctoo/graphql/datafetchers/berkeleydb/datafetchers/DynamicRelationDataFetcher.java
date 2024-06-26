package nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.datafetchers;

import com.google.common.collect.Lists;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.PaginationArgumentsHelper;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.PaginatedDynamicList;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.TypedLanguageValue;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.TypedValue;

import java.util.Optional;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.datafetchers.PaginationHelper
  .getPaginatedList;

public class DynamicRelationDataFetcher implements DataFetcher<PaginatedDynamicList> {
  private final PaginationArgumentsHelper argumentsHelper;

  public DynamicRelationDataFetcher(PaginationArgumentsHelper argumentsHelper) {
    this.argumentsHelper = argumentsHelper;
  }

  private DatabaseResult makeItem(CursorQuad quad, DataSet dataSet) {
    if (quad.getValuetype().isPresent()) {
      if (quad.getLanguage().isPresent()) {
        return TypedLanguageValue.create(quad.getObject(), quad.getValuetype().get(),
            quad.getLanguage().get(), dataSet);
      } else {
        return TypedValue.create(quad.getObject(), quad.getValuetype().get(), dataSet);
      }
    } else {
      return new LazyTypeSubjectReference(quad.getObject(), Optional.empty(), dataSet);
    }
  }

  @Override
  public PaginatedDynamicList get(DataFetchingEnvironment environment) {
    if (environment.getSource() instanceof SubjectReference) {
      SubjectReference source = environment.getSource();
      PaginationArguments arguments = argumentsHelper.getPaginationArguments(environment);
      DataSet dataSet = ((DatabaseResult) environment.getSource()).getDataSet();
      String cursor = arguments.getCursor();
      String predicate = environment.getArgument("uri");
      Direction direction = environment.getArgument("outgoing") ? Direction.OUT : Direction.IN;

      try (Stream<CursorQuad> q =
               dataSet.getQuadStore().getQuads(source.getSubjectUri(), predicate, direction, cursor)) {
        final PaginatedList<DatabaseResult> paginatedList = getPaginatedList(
          q,
          qd -> this.makeItem(qd, dataSet),
          arguments,
          Optional.empty()
        );
        return PaginatedDynamicList.create(
          paginatedList.getPrevCursor(),
          paginatedList.getNextCursor(),
          paginatedList.getItems()
        );
      }
    } else {
      return PaginatedDynamicList.create(
        Optional.empty(),
        Optional.empty(),
        Lists.newArrayList()
      );
    }
  }
}
