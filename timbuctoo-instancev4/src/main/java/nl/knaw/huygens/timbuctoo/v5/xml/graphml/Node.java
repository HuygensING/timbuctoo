package nl.knaw.huygens.timbuctoo.v5.xml.graphml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Node {

  public static final String LOCAL_NAME = "node";
  public static final QName QNAME = new QName(Gml.NAMESPACE, LOCAL_NAME);

  @XmlAttribute() private String id;

  @XmlElement(name = Data.LOCAL_NAME, namespace = Gml.NAMESPACE)
  private List<Data> dataList = new ArrayList<>();

  public Node() {}

  public Node(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public Node withId(String id) {
    this.id = id;
    return this;
  }

  public Node addData(Data data) {
    dataList.add(data);
    return this;
  }

  public List<Data> getDataList() {
    return dataList;
  }

  public void setDataList(List<Data> dataList) {
    this.dataList = dataList;
  }

  public JAXBElement<Node> asJaxbElement() {
    return new JAXBElement<Node>(QNAME, Node.class,this);
  }
}
