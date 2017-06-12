package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.PalladioCsvSerialization;

import javax.ws.rs.Produces;

@Produces("text/palladio+csv")
public class PalladioCsvWriter extends SerializerWriter {

  public PalladioCsvWriter() {
    super(PalladioCsvSerialization::new);
  }
}
