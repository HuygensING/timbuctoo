package nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto;

import java.util.Set;

public interface SubjectReference extends DatabaseResult {
  String getSubjectUri();

  Set<String> getTypes();
}
