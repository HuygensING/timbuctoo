package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.XmlSerialization;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_XML)
public class XmlWriter extends SerializerWriter {
  public XmlWriter() {
    super(XmlSerialization::new);
  }
}
