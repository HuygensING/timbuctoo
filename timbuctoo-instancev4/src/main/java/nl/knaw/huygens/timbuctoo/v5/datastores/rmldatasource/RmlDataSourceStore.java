package nl.knaw.huygens.timbuctoo.v5.datastores.rmldatasource;

import java.util.stream.Stream;

public interface RmlDataSourceStore {
  Stream<String> get(String collectionUri);
}
