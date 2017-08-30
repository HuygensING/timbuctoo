package nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync;

public class ResourceSyncException extends Exception {
  public ResourceSyncException(Exception wrappedException) {
    super(wrappedException);
  }
}
