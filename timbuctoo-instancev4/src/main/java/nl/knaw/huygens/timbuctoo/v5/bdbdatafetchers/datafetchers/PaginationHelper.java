package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorContainer;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaginationHelper {

  public static final int MAX_COUNT = 10_000;

  static <T extends CursorContainer> PaginatedList getPaginatedList(Stream<T> subjectStream,
                                                                    Function<T, TypedValue> makeItem, int count) {
    String[] cursors = new String[2];

    List<TypedValue> subjects = subjectStream
      .limit((count < 0 || count > MAX_COUNT) ? MAX_COUNT : count)
      .peek(cs -> {
        if (cursors[0] == null) {
          cursors[0] = "D\n" + cs.getCursor();
        }
        cursors[1] = "A\n" + cs.getCursor();
      })
      .map(makeItem)
      .collect(Collectors.toList());

    if (subjects.isEmpty()) {
      return PaginatedList.create(
        "NONE",
        "",
        subjects
      );
    } else {
      return PaginatedList.create(
        cursors[0],
        cursors[1],
        subjects
      );
    }
  }

}
