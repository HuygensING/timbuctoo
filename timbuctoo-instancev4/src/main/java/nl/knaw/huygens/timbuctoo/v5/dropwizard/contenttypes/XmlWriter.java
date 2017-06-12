package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.XmlSerialization;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(XmlWriter.MIME_TYPE)
public class XmlWriter extends SerializerWriter {

  public static final String MIME_TYPE = MediaType.APPLICATION_XML;

  public XmlWriter() {
    super(XmlSerialization::new);
  }

  @Override
  public String getMimeType() {
    return MIME_TYPE;
  }
}
