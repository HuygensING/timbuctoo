package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.handle.HandleAdderParameters;

// TODO make more generic PidCreator
public interface HandleCreator {
  void add(HandleAdderParameters params);
}
