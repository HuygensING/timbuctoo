package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;


import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;

public interface DataFetcherFactory {
  RelatedDataFetcher<SubjectReference> relationFetcher(String predicate, Direction direction);

  RelatedDataFetcher<TypedValue> typedLiteralFetcher(String predicate);

  RelatedDataFetcher<DatabaseResult> unionFetcher(String predicate, Direction direction);

  CollectionFetcher collectionFetcher(String typeUri);

  LookUpSubjectByUriFetcher lookupFetcher();
}
