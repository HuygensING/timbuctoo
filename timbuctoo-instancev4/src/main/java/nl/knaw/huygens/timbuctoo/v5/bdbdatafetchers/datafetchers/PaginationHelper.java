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
                                                                    Function<T, TypedValue> makeItem, int count,
                                                                    boolean startedFromCursor) {
    String[] cursors = new String[3];

    if (count < 0 || count > MAX_COUNT) {
      count = MAX_COUNT;
    }
    count += 1; //to determine if we reached the end of the list we keep track of one extra
    List<TypedValue> subjects = subjectStream
      .limit(count)
      .peek(cs -> {
        if (cursors[0] == null) {
          cursors[0] = "D\n" + cs.getCursor();
        }
        cursors[1] = cursors[2]; //keep track of both the cursor of the last item and the before-last item
        cursors[2] = "A\n" + cs.getCursor();
      })
      .map(makeItem)
      .collect(Collectors.toList());

    if (subjects.isEmpty()) {
      return PaginatedList.create(
        null,
        null,
        subjects
      );
    } else {
      return PaginatedList.create(
        startedFromCursor ? cursors[0] : null,
        subjects.size() == count ? cursors[1] : null,
        subjects.size() == count ? subjects.subList(0, count - 1) : subjects //remove the one extra
      );
    }
  }

}
