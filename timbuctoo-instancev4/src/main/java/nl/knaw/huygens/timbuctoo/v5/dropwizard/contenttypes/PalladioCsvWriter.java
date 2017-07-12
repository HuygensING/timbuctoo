package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.PalladioCsvSerialization;

import javax.ws.rs.Produces;

@Produces(PalladioCsvWriter.MIME_TYPE)
public class PalladioCsvWriter extends SerializerWriter {

  public static final String MIME_TYPE = "text/palladio+csv";

  public PalladioCsvWriter() {
    super(PalladioCsvSerialization::new);
  }

  @Override
  public String getMimeType() {
    return MIME_TYPE;
  }
}
