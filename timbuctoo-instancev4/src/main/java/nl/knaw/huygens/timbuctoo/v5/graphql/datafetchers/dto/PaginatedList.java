package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import nl.knaw.huygens.timbuctoo.util.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class PaginatedList {
  List<Edge> edges;
  PageInfo pageInfo;

  public <T> PaginatedList(Function<T, BoundSubject> mapper,
                       BiFunction<Boolean, String, Stream<Tuple<String, T>>> streamSupplier, String after,
                       String before, Integer first, Integer last) {
    int count;
    boolean ascending;
    String cursor;

    if (after != null) {
      if (first != null) {
        count = first;
      } else {
        count = 20;
      }
      cursor = after;
      ascending = true;

    } else {
      if (before != null) {
        if (last != null) {
          count = last;
        } else {
          count = 20;
        }
        cursor = before;
        ascending = false;
      } else {
        if (first != null) {
          count = first;
        } else {
          count = 20;
        }
        cursor = null;
        ascending = true;
      }
    }
    if (count > 20) {
      count = 20;
    }

    edges = new ArrayList<>();
    pageInfo = new PageInfo(false, null, false, null);
    try (Stream<Tuple<String, T>> items = streamSupplier.apply(ascending, cursor)) {
      edges = items
        .filter(Objects::nonNull)
        .limit(count + 1)
        .map(s -> new Edge(mapper.apply(s.getRight()), s.getLeft()))
        .collect(toList());
      boolean hasNextPage = false;
      if (edges.size() > count) {
        hasNextPage = true;
        edges.remove(edges.size() - 1);
      }
      pageInfo = new PageInfo(
        ascending && hasNextPage,
        ascending && hasNextPage ? edges.get(edges.size() - 1).getCursor() : null,
        !ascending && hasNextPage,
        !ascending && hasNextPage ? edges.get(edges.size() - 1).getCursor() : null
      );
    }
  }

  public List<Edge> getEdges() {
    return edges;
  }

  public PageInfo getPageInfo() {
    return pageInfo;
  }

}

