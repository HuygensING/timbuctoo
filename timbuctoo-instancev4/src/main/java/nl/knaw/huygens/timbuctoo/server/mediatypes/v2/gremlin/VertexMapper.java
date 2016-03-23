package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.Datable;
import nl.knaw.huygens.timbuctoo.model.LocationNames;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.description.PropertyDescriptor;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VertexMapper {
  private static final PropertyDescriptorFactory propertyDescriptorFactory;
  private static final PropertyParserFactory propertyParserFactory;

  static {
    propertyParserFactory = new PropertyParserFactory();
    propertyDescriptorFactory = new PropertyDescriptorFactory(propertyParserFactory);
  }

  private static String getVertexType(Vertex vertex)  {
    String types = (String) vertex.property("types").value();
    ObjectMapper mapper = new ObjectMapper();


    List<String> typeList = null;
    try {
      typeList = mapper.readValue(types,
              mapper.getTypeFactory().constructCollectionType(List.class, String.class));
    } catch (IOException e) {
      typeList = new ArrayList<>();
      typeList.add("");
    }

    typeList.sort((o1, o2) -> o1.length() - o2.length());
    return typeList.get(0);
  }

  private static PropertyDescriptor createAuthorDescriptor() {
    PropertyDescriptor authorNameDescriptor = propertyDescriptorFactory.getDerivedWithSeparator(
            "isCreatedBy",
            "wwperson_names",
            propertyParserFactory.getParser(PersonNames.class),
            "; ");
    PropertyDescriptor authorTempNameDescriptor = propertyDescriptorFactory.getDerivedWithSeparator(
            "isCreatedBy",
            "wwperson_tempName",
            propertyParserFactory.getParser(String.class),
            "; ");

    return propertyDescriptorFactory
            .getComposite(authorNameDescriptor, authorTempNameDescriptor);
  }

  public static EntityRef mapVertex(Vertex vertex) {
    String id = (String) vertex.property("tim_id").value();
    String type = getVertexType(vertex);
    String displayName = "";
    Map<String, Object> data = Maps.newHashMap();

    if (type.equals("person")) {
      displayName = propertyDescriptorFactory.getComposite(
              propertyDescriptorFactory.getLocal("wwperson_names", PersonNames.class),
              propertyDescriptorFactory.getLocal("wwperson_tempName", String.class)).get(vertex);
    } else if (type.equals("location")) {
      displayName = propertyDescriptorFactory.getLocal("names", LocationNames.class).get(vertex);
    } else if (type.equals("document")) {
      PropertyDescriptor titleDescriptor = propertyDescriptorFactory.getLocal("wwdocument_title", String.class);
      PropertyDescriptor dateDescriptor = propertyDescriptorFactory
              .getLocal("wwdocument_date", Datable.class, "(", ")");

      PropertyDescriptor documentDescriptor = propertyDescriptorFactory
              .getAppender(titleDescriptor, dateDescriptor, " ");

      displayName = propertyDescriptorFactory.getAppender(createAuthorDescriptor(), documentDescriptor, " - ")
              .get(vertex);
    } else if (type.equals("keyword")) {
      PropertyDescriptor typeDescriptor = propertyDescriptorFactory.getLocal("keyword_type", String.class);
      PropertyDescriptor valueDescriptor = propertyDescriptorFactory.getLocal("keyword_value", String.class);
      displayName = propertyDescriptorFactory.getAppender(valueDescriptor, typeDescriptor, " - ")
              .get(vertex);
    } else if (type.equals("collective")) {
      PropertyDescriptor typeDescriptor = propertyDescriptorFactory.getLocal("wwcollective_type", String.class);
      PropertyDescriptor valueDescriptor = propertyDescriptorFactory.getLocal("wwcollective_name", String.class);
      displayName = propertyDescriptorFactory.getAppender(valueDescriptor, typeDescriptor, " - ")
              .get(vertex);
    } else if (type.equals("language")) {
      displayName = propertyDescriptorFactory.getLocal("language_name", String.class).get(vertex);
    }


    EntityRef ref = new EntityRef(type, id);
    ref.setDisplayName(displayName);
    ref.setData(data);

    return ref;
  }
}
