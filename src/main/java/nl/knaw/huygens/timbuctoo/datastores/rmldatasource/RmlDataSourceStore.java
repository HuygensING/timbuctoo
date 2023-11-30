package nl.knaw.huygens.timbuctoo.datastores.rmldatasource;

import nl.knaw.huygens.timbuctoo.dataset.OptimizedPatchListener;

import java.util.stream.Stream;

public interface RmlDataSourceStore extends OptimizedPatchListener {
  Stream<String> get(String collectionUri);

  void close();

  boolean isClean();
}
