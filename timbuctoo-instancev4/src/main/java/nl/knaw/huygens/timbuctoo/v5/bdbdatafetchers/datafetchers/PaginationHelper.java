package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorContainer;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaginationHelper {
  static <T extends CursorContainer> PaginatedList getPaginatedList(Stream<T> subjectStream,
                                                                    Function<T, TypedValue> makeItem) {
    String[] cursors = new String[2];

    List<TypedValue> subjects = subjectStream
      .limit(20)
      .peek(cs -> {
        if (cursors[0] == null) {
          cursors[0] = cs.getCursor();
        }
        cursors[1] = cs.getCursor();
      })
      .map(makeItem)
      .collect(Collectors.toList());
    return PaginatedList.create(
      cursors[0],
      cursors[1],
      subjects
    );
  }

}
