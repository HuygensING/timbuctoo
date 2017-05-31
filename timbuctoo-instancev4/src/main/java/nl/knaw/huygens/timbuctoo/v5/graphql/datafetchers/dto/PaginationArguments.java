package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import org.immutables.value.Value;

@Value.Immutable
public interface PaginationArguments {
  String getCursor();

  int getCount();


}
