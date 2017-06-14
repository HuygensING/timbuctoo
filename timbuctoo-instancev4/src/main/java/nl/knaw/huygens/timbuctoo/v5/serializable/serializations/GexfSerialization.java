package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.TocGenerator;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.Edge;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.Entity;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.EntityFirstSerialization;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created on 2017-06-12 14:21.
 */
public class GexfSerialization extends EntityFirstSerialization {

  public static final String GEXF_NAMESPACE = "http://www.gexf.net/1.2draft";
  public static final String GEXF_SCHEMA_LOCATION = "http://www.gexf.net/1.2draft/gexf.xsd";
  public static final String GEXF_ROOT_ELEMENT = "gexf";

  private final XMLStreamWriter xsw;

  private boolean elementNodesStarted;
  private boolean elementEdgesStarted;

  public GexfSerialization(OutputStream outputStream) throws IOException {
    XMLOutputFactory xof = XMLOutputFactory.newFactory();
    try {
      xsw = new IndentingXMLStreamWriter(xof.createXMLStreamWriter(outputStream, StandardCharsets.UTF_8.name()));
      xsw.setDefaultNamespace(GEXF_NAMESPACE);
      xsw.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
      xsw.writeStartElement(GEXF_NAMESPACE, GEXF_ROOT_ELEMENT);
      xsw.writeDefaultNamespace(GEXF_NAMESPACE);
      xsw.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
      xsw.writeAttribute("xsi:schemaLocation", GEXF_NAMESPACE + " " + GEXF_SCHEMA_LOCATION);
      xsw.writeAttribute("version", "1.2");

      xsw.writeStartElement(GEXF_NAMESPACE, "graph");
      xsw.writeAttribute("defaultedgetype", "directed");
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void initialize(TocGenerator tocGenerator, TypeNameStore typeNameStore) throws IOException {
    super.initialize(tocGenerator, typeNameStore);
    try {
      // attributes for nodes
      xsw.writeStartElement(GEXF_NAMESPACE, "attributes");
      xsw.writeAttribute("class", "node");

      xsw.writeStartElement(GEXF_NAMESPACE, "attribute");
      xsw.writeAttribute("id", "label");
      xsw.writeAttribute("title", "label");
      xsw.writeAttribute("type", "string");
      xsw.writeEndElement(); // attribute

      // xsw.writeStartElement(GEXF_NAMESPACE, "attribute");
      // xsw.writeAttribute("id", "node_id");
      // xsw.writeAttribute("title", "node_id");
      // xsw.writeAttribute("type", "string");
      // xsw.writeEndElement(); // attribute

      for (String field : getLeafFieldNames()) {
        String propertyName = getTypeNameStore().makeGraphQlname(field);
        xsw.writeStartElement(GEXF_NAMESPACE, "attribute");
        xsw.writeAttribute("id", propertyName);
        xsw.writeAttribute("title", propertyName);
        xsw.writeAttribute("type", "string");
        xsw.writeEndElement(); // attribute
      }
      xsw.writeEndElement(); // attributes for nodes

      // attributes for edges
      xsw.writeStartElement(GEXF_NAMESPACE, "attributes");
      xsw.writeAttribute("class", "edge");

      xsw.writeStartElement(GEXF_NAMESPACE, "attribute");
      xsw.writeAttribute("id", "label");
      xsw.writeAttribute("title", "label");
      xsw.writeAttribute("type", "string");
      xsw.writeEndElement(); // attribute

      xsw.writeEndElement(); // attributes for edges

    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void onDistinctEntity(Entity entity) throws IOException {
    try {
      if (elementEdgesStarted) {
        xsw.writeEndElement(); // edges
        elementEdgesStarted = false;
      }
      if (!elementNodesStarted) {
        xsw.writeStartElement(GEXF_NAMESPACE, "nodes");
        elementNodesStarted = true;
      }
      xsw.writeStartElement(GEXF_NAMESPACE, "node");
      String shortUri = getTypeNameStore().shorten(entity.getUri());
      xsw.writeAttribute("id", shortUri);
      xsw.writeStartElement(GEXF_NAMESPACE, "attvalues");

      // xsw.writeStartElement(GEXF_NAMESPACE, "attvalue");
      // xsw.writeAttribute("for", "id");
      // xsw.writeAttribute("value", entity.getUri());
      // xsw.writeEndElement(); // attvalue

      xsw.writeStartElement(GEXF_NAMESPACE, "attvalue");
      xsw.writeAttribute("for", "label");
      xsw.writeAttribute("value", shortUri);
      xsw.writeEndElement(); // attvalue

      for (Edge edge : entity.getOutEdges()) {
        if (edge.isValueEdge()) {
          String propertyName = getTypeNameStore().makeGraphQlname(edge.getName());
          xsw.writeStartElement(GEXF_NAMESPACE, "attvalue");
          xsw.writeAttribute("for", propertyName);
          xsw.writeAttribute("value", edge.getTargetAsString());
          xsw.writeEndElement(); // attvalue
        }
      }
      xsw.writeEndElement(); // attvalues
      xsw.writeEndElement(); // node
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
    super.onDistinctEntity(entity);
  }

  @Override
  public void onDeclaredEntityEdge(Edge edge) throws IOException {
    if (edge.isNodeEdge()) {
      try {
        if (elementNodesStarted) {
          xsw.writeEndElement(); // nodes
          elementNodesStarted = false;
        }
        if (!elementEdgesStarted) {
          xsw.writeStartElement(GEXF_NAMESPACE, "edges");
          elementEdgesStarted = true;
        }
        String propertyName = getTypeNameStore().makeGraphQlname(edge.getName());
        String shortSource = getTypeNameStore().shorten(edge.getSourceUri());
        String shortTarget = getTypeNameStore().shorten(edge.getTargetUri());
        xsw.writeStartElement(GEXF_NAMESPACE, "edge");
        xsw.writeAttribute("id", edge.getId());
        xsw.writeAttribute("source", shortSource);
        xsw.writeAttribute("target", shortTarget);
        xsw.writeStartElement(GEXF_NAMESPACE, "attvalues");
        xsw.writeStartElement(GEXF_NAMESPACE, "attvalue");
        xsw.writeAttribute("for", "label");
        xsw.writeAttribute("value", propertyName);
        xsw.writeEndElement(); // attvalue
        xsw.writeEndElement(); // attvalues
        xsw.writeEndElement(); // edge
      } catch (XMLStreamException e) {
        throw new IOException(e);
      }
    }
  }

  @Override
  public void finish() throws IOException {
    super.finish();
    try {
      if (elementEdgesStarted) {
        xsw.writeEndElement(); // edges
      }
      if (elementNodesStarted) {
        xsw.writeEndElement(); // nodes
      }
      xsw.writeEndElement(); // graph
      xsw.writeEndElement(); // gexf
      xsw.writeEndDocument();
      xsw.close();
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }


}
