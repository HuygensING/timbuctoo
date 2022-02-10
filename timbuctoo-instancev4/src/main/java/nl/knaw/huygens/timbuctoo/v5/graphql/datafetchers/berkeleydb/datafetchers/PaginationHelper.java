package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.updatedperpatchstore.SubjectCursor;
import nl.knaw.huygens.timbuctoo.v5.datastores.CursorValue;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaginationHelper<S extends CursorValue, U> {
  public static final int MAX_COUNT = 10_000;

  private final Stream<S> subjectStream;
  private final Function<S, U> makeItem;
  private final int count;
  private final Optional<Long> total;
  private final String cursor;

  private PaginationHelper(Stream<S> subjectStream, Function<S, U> makeItem, int count,
                           Optional<Long> total, String cursor) {
    this.subjectStream = subjectStream;
    this.makeItem = makeItem;

    if (count < 0 || count > MAX_COUNT) {
      count = MAX_COUNT;
    }
    this.count = count + 1; // To determine if we reached the end of the list we keep track of one extra

    this.total = total;
    this.cursor = cursor;
  }

  private PaginatedList<U> getPaginatedList() {
    String[] cursors = new String[3];

    List<U> subjects = subjectStream
        .limit(count)
        .peek(cs -> {
          if (cursors[0] == null) {
            cursors[0] = cs.getCursor();
          }
          cursors[1] = cursors[2]; // Keep track of both the cursor of the last item and the before-last item
          cursors[2] = cs.getCursor();
        })
        .map(makeItem)
        .collect(Collectors.toList());

    if (subjects.isEmpty()) {
      return PaginatedList.create(
          null,
          null,
          subjects,
          total
      );
    }

    final List<U> items = subjects.size() == count ?
        subjects.subList(0, count - 1) : // Remove the one extra
        subjects;

    String prevCursor;
    String nextCursor;
    if (cursor.startsWith("D") || cursor.equals("LAST")) {
      Collections.reverse(items);
      // We're iterating downwards so the cursor as retrieved from the end of the list
      // is the prevCursor, not the nextCursor
      prevCursor = subjects.size() == count ? "D\n" + cursors[1] : null;
      nextCursor = cursor.equals("LAST") ? null : "A\n" + cursors[0];
    } else {
      prevCursor = cursor.equals("") ? null : "D\n" + cursors[0];
      nextCursor = subjects.size() == count ? "A\n" + cursors[1] : null;
    }

    return PaginatedList.create(
        prevCursor,
        nextCursor,
        items,
        total);
  }

  static <U> PaginatedList<U> getPaginatedList(
      Stream<CursorQuad> subjectStream, Function<CursorQuad, U> makeItem,
      PaginationArguments arguments, Optional<Long> total) {
    return new PaginationHelper<CursorQuad, U>(
        subjectStream, makeItem, arguments.getCount(), total, arguments.getCursor()).getPaginatedList();
  }

  static <U> PaginatedList<U> getUpdatedPaginatedList(
      Stream<SubjectCursor> subjectStream, Function<SubjectCursor, U> makeItem, PaginationArguments arguments) {
    return new PaginationHelper<SubjectCursor, U>(
        subjectStream, makeItem, arguments.getCount(), Optional.empty(), arguments.getCursor()).getPaginatedList();
  }

  static <U, S extends CursorValue> PaginatedList<U> getPaginatedList(
      Stream<S> subjectStream, Function<S, U> makeItem, PaginationArguments arguments, Optional<Long> total) {
    return new PaginationHelper<S, U>(
        subjectStream, makeItem, arguments.getCount(), total, arguments.getCursor()).getPaginatedList();
  }
}
