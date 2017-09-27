package nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex;

import java.util.stream.Stream;

// CollectionIndex
public interface CollectionIndex {
  Stream<CursorSubject> getSubjects(String collectionName, String cursor);
}
