package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.timbuctoo.v5.serializable.dto.Value;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.CollectionsOfEntitiesSerialization;

public class GraphMlSerialization extends CollectionsOfEntitiesSerialization {

  //  private final CSVPrinter csvPrinter;

  public GraphMlSerialization(OutputStream outputStream) throws IOException {
    //    csvPrinter = new CSVPrinter(new PrintWriter(outputStream), CSVFormat.EXCEL);
  }

  protected void initialize() throws IOException {
  }
}
