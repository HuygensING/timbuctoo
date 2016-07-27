package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;

import java.util.Arrays;
import java.util.Iterator;

public class RmlMappingDocument implements Iterable<RrTriplesMap> {

  private final RrTriplesMap[] triplesMaps;

  public RmlMappingDocument(RrTriplesMap... triplesMaps) {
    this.triplesMaps = triplesMaps;
  }

  @Override
  public Iterator<RrTriplesMap> iterator() {
    return Arrays.stream(triplesMaps).iterator();
  }

}
