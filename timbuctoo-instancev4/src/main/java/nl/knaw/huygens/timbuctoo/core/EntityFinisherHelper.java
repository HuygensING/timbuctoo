package nl.knaw.huygens.timbuctoo.core;

import nl.knaw.huygens.timbuctoo.model.Change;

import java.time.Clock;
import java.util.UUID;

public class EntityFinisherHelper {

  private final Change change;

  // TODO let TimbuctooActions use this class
  public EntityFinisherHelper() {
    change = new Change();
    change.setTimeStamp(Clock.systemDefaultZone().instant().toEpochMilli()); // TODO make configurable
    change.setUserId("rdfImporter"); // TODO make configurable
  }

  public UUID newId() {
    return UUID.randomUUID();
  }

  public int getRev() {
    return 1;
  }

  public Change getChangeTime() {
    return change;
  }
}
