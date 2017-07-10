package nl.knaw.huygens.timbuctoo.v5.dataset;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorSubject;

import java.util.stream.Stream;

// CollectionIndex
public interface CollectionIndex {
  Stream<CursorSubject> getSubjects(String collectionName, String cursor);
}
