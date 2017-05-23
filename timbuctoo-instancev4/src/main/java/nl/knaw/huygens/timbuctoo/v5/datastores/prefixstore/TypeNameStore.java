package nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore;

import nl.knaw.huygens.timbuctoo.v5.datastores.DataStore;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadLoader;

import java.util.Map;

public interface TypeNameStore extends AutoCloseable, DataStore<QuadLoader> {
  //I think that a fully reversable shortened version looks ugly. And usually this is not needed
  //So I shorten by throwing away information and use a HashMap to be able to revert the process
  //and prevent collisions.
  String makeRelayCompatibleGraphQlname(String uri);

  String makeUri(String graphQlName);

  String shorten(String uri);

  Map<String, String> getMappings();

  void addPrefix(String prefix, String iri);
}
