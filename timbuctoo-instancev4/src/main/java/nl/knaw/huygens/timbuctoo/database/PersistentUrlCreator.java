package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.database.dto.EntityLookup;

import java.net.URI;

public interface PersistentUrlCreator {
  void add(URI uriToRedirectTo, EntityLookup entityLookup);
}
