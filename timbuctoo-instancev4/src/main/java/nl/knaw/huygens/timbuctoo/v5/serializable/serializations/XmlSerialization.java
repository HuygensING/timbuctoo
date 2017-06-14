package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serialization;
import nl.knaw.huygens.timbuctoo.v5.serializable.TocGenerator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class XmlSerialization implements Serialization {

  private final IndentingXMLStreamWriter indentingXmlStreamWriter;
  private TypeNameStore typeNameStore;

  public XmlSerialization(OutputStream outputStream) throws IOException {
    XMLOutputFactory xof = XMLOutputFactory.newFactory();
    try {
      indentingXmlStreamWriter =
        new IndentingXMLStreamWriter(xof.createXMLStreamWriter(outputStream, StandardCharsets.UTF_8.name()));
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void initialize(TocGenerator tocGenerator, TypeNameStore typeNameStore) throws IOException {
    this.typeNameStore = typeNameStore;
    writeXml(indentingXmlStreamWriter::writeStartDocument);
  }

  @Override
  public void finish() throws IOException {
    writeXml(() -> {
      indentingXmlStreamWriter.writeEndDocument();
      indentingXmlStreamWriter.flush();
    });
  }

  @Override
  public void onStartEntity(String uri) throws IOException {
    final String nonNullUri = uri != null ? uri : "http://timbuctoo.com/unknown_uri/" + UUID.randomUUID();
    writeXml(() -> {
      indentingXmlStreamWriter.writeEmptyElement(typeNameStore.shorten(nonNullUri));
      if (uri != null) {
        indentingXmlStreamWriter.writeAttribute("uri", nonNullUri);
      }
    });
  }

  @Override
  public void onProperty(String propertyName) throws IOException {
    writeXml(() -> indentingXmlStreamWriter.writeStartElement(propertyName));
  }

  @Override
  public void onCloseEntity(String uri) throws IOException {
    writeXml(indentingXmlStreamWriter::writeEndElement);
  }

  @Override
  public void onStartList() throws IOException {
    writeXml(() -> indentingXmlStreamWriter.writeStartElement("items"));
  }

  @Override
  public void onListItem(int index) throws IOException {
    writeXml(() -> indentingXmlStreamWriter.writeStartElement("" + index));
  }

  @Override
  public void onCloseList() throws IOException {
    writeXml(() -> indentingXmlStreamWriter.writeEndElement());
  }

  @Override
  public void onRdfValue(Object value, String valueType) throws IOException {
    writeXml(() -> {
      indentingXmlStreamWriter.writeCharacters("" + value);
      indentingXmlStreamWriter.writeEndElement(); // close property
    });
  }

  @Override
  public void onValue(Object value) throws IOException {
    writeXml(() -> {
      indentingXmlStreamWriter.writeStartElement("" + value);
      indentingXmlStreamWriter.writeEndElement(); // close value
      indentingXmlStreamWriter.writeEndElement(); // close property
    });
  }

  private interface WriteXmlAction {
    void write() throws XMLStreamException;
  }

  private void writeXml(WriteXmlAction action) throws IOException {
    try {
      action.write();
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }
}
