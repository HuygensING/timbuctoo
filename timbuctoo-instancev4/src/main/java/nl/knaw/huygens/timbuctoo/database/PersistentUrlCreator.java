package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.handle.HandleAdderParameters;

// TODO make more generic PidCreator
public interface PersistentUrlCreator {
  void add(HandleAdderParameters params);
}
