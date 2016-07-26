package nl.knaw.huygens.timbuctoo.rml;

import java.util.Arrays;
import java.util.Iterator;

public class RmlMappingDocument implements Iterable<TriplesMap> {

  private final TriplesMap[] triplesMaps;

  public RmlMappingDocument(TriplesMap... triplesMaps) {
    this.triplesMaps = triplesMaps;
  }

  @Override
  public Iterator<TriplesMap> iterator() {
    return Arrays.stream(triplesMaps).iterator();
  }

}
