package nl.knaw.huygens.timbuctoo.implementations;

import nl.knaw.huygens.timbuctoo.rdfio.implementations.BasicRdfPatchSerializer;

import java.util.function.Consumer;

public class TestBasicRdfPatchSerializer extends BasicRdfPatchSerializer {
  public TestBasicRdfPatchSerializer(Consumer<String> printWriter) {
    super(printWriter);
  }
}
