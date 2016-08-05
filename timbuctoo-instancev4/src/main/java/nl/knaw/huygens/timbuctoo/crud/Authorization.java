package nl.knaw.huygens.timbuctoo.crud;

import java.util.List;

public interface Authorization {
  List<String> getRoles();

  boolean isAllowedToWrite();
}
