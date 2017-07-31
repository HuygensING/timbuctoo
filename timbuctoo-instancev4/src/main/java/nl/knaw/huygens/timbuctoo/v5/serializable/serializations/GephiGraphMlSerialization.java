package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;


import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.TocGenerator;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.Edge;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.Entity;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.EntityFirstSerialization;
import nl.knaw.huygens.timbuctoo.v5.xml.graphml.Data;
import nl.knaw.huygens.timbuctoo.v5.xml.graphml.Gml;
import nl.knaw.huygens.timbuctoo.v5.xml.graphml.GmlEdge;
import nl.knaw.huygens.timbuctoo.v5.xml.graphml.Graph;
import nl.knaw.huygens.timbuctoo.v5.xml.graphml.GraphMlContext;
import nl.knaw.huygens.timbuctoo.v5.xml.graphml.Key;
import nl.knaw.huygens.timbuctoo.v5.xml.graphml.Node;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created on 2017-06-07 13:16.
 */
public class GephiGraphMlSerialization extends EntityFirstSerialization {

  private static final String ID_NODE_LABEL = "label";
  private static final String ID_EDGE_LABEL = "edgelabel";

  private final XMLStreamWriter xsw;
  private final Marshaller marshaller;

  public GephiGraphMlSerialization(OutputStream outputStream) throws IOException {
    XMLOutputFactory xof = XMLOutputFactory.newFactory();
    try {
      xsw = new IndentingXMLStreamWriter(xof.createXMLStreamWriter(outputStream, StandardCharsets.UTF_8.name()));
      xsw.setDefaultNamespace(Gml.NAMESPACE);
      xsw.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
      xsw.writeStartElement(Gml.NAMESPACE, Gml.LOCAL_NAME);
      xsw.writeDefaultNamespace(Gml.NAMESPACE);
      GraphMlContext graphMlContext = new GraphMlContext();
      marshaller = graphMlContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
    } catch (XMLStreamException | JAXBException e1) {
      throw new IOException(e1);
    }
  }

  @Override
  public void initialize(TocGenerator tocGenerator, TypeNameStore typeNameStore) throws IOException {
    super.initialize(tocGenerator, typeNameStore);
    try {
      marshaller.marshal(new Key(ID_EDGE_LABEL).withFor("edge").withAttrName("EdgeLabel").withAttrType("string")
                                               .asJaxbElement(), xsw);
      marshaller.marshal(new Key(ID_NODE_LABEL).withFor("node").withAttrName("Label").withAttrType("string")
                                               .asJaxbElement(), xsw);
      for (String field : getLeafFieldNames()) {
        String propertyName = getTypeNameStore().makeGraphQlname(field);
        marshaller.marshal(new Key(propertyName).withFor("node").withAttrName(propertyName).withAttrType("string")
                                                 .asJaxbElement(), xsw);
      }
      xsw.writeStartElement(Gml.NAMESPACE, Graph.LOCAL_NAME);
      xsw.writeAttribute("edgedefault", "directed");
    } catch (JAXBException | XMLStreamException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void onDistinctEntity(Entity entity) throws IOException {
    String shortSource = getTypeNameStore().shorten(entity.getUri());
    Node node = new Node(shortSource);
    for (Edge edge : entity.getOutEdges()) {
      if (edge.isValueEdge()) {
        String propertyName = getTypeNameStore().makeGraphQlname(edge.getName());
        node.addData(new Data().withKey(propertyName).withValue(edge.getTarget().toString()));
      }
    }
    try {
      marshaller.marshal(node.asJaxbElement(), xsw);
    } catch (JAXBException e) {
      throw new IOException(e);
    }
    super.onDistinctEntity(entity);
  }

  @Override
  public void onDeclaredEntityEdge(Edge edge) throws IOException {
    if (edge.isNodeEdge()) {
      String propertyName = getTypeNameStore().makeGraphQlname(edge.getName());
      String shortSource = getTypeNameStore().shorten(edge.getSourceUri());
      String shortTarget = getTypeNameStore().shorten(edge.getTargetUri());
      GmlEdge gmlEdge = new GmlEdge(edge.getId())
        .addData(new Data().withKey(ID_EDGE_LABEL).withValue(propertyName))
        .withSource(shortSource)
        .withTarget(shortTarget);
      try {
        marshaller.marshal(gmlEdge.asJaxbElement(), xsw);
      } catch (JAXBException e) {
        throw new IOException(e);
      }
    }
  }

  @Override
  public void finish() throws IOException {
    super.finish();
    try {
      xsw.writeEndElement(); // graph
      xsw.writeEndElement(); // graphml
      xsw.writeEndDocument();
      xsw.close();
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }

}
