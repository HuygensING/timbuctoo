package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.handle.HandleAdderParameters;

// TODO make more generic PidCreator
// TODO find a better name for the class
public interface HandleCreator {
  void add(HandleAdderParameters params);
}
