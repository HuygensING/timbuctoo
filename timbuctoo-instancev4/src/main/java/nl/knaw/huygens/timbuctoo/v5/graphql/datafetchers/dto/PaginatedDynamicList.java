package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders.CursorList;
import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface PaginatedDynamicList extends CursorList {
  List<SubjectReference> getEntities();

  List<TypedValue> getValues();

  static PaginatedDynamicList create(Optional<String> prevCursor, Optional<String> nextCursor,
                                     List<DatabaseResult> items) {
    List<SubjectReference> entities = new ArrayList<>();
    List<TypedValue> values = new ArrayList<>();

    for (DatabaseResult item : items) {
      if (item instanceof TypedValue) {
        values.add((TypedValue) item);
      } else if (item instanceof SubjectReference) {
        entities.add((SubjectReference) item);
      } else {
        throw new IllegalStateException("Item should be either a SubjectReference or a TypedValue");
      }
    }

    return ImmutablePaginatedDynamicList.builder()
      .prevCursor(prevCursor.map(CursorList::encode))
      .nextCursor(nextCursor.map(CursorList::encode))
      .entities(entities)
      .values(values)
      .build();
  }
}
