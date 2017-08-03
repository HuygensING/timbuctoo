package nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore;

import java.util.Map;

public interface TypeNameStore extends AutoCloseable {
  //I think that a fully reversable shortened version looks ugly. And usually this is not needed
  //So I shorten by throwing away information and use a HashMap to be able to revert the process
  //and prevent collisions.
  String makeGraphQlname(String uri);

  //Does the same thing, but a type may be used as a value type or an entity type and the graphql names shouldn't clash
  //so this method prefixes the name with value_
  String makeGraphQlValuename(String uri);

  String makeUri(String graphQlName);

  String shorten(String uri);

  Map<String, String> getMappings();
}
