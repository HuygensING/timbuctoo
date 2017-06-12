package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.CsvSerialization;

import javax.ws.rs.Produces;

@Produces("text/csv")
public class CsvWriter extends SerializerWriter {
  public CsvWriter() {
    super(CsvSerialization::new);
  }
}
