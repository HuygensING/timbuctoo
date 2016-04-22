package nl.knaw.huygens.timbuctoo.search.description.indexes;


import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.Datable;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.TempNamePropertyParser;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class PersonIndexDescription implements IndexDescription {

  private static final String SORT_POSTFIX = "sort";

  private static final String[] SORT_FIELDS = {
    "names",
    "deathDate",
    "birthDate",
    "modified"
  };

  private final List<String> types;
  private final PropertyParserFactory propertyParserFactory;
  private final HashMap<String, PropertyParser> parsers;

  public PersonIndexDescription(List<String> types) {
    this.types = types;
    this.propertyParserFactory = new PropertyParserFactory();
    this.parsers = Maps.newHashMap();
    parsers.put("names", propertyParserFactory.getParser(PersonNames.class));
    parsers.put("deathDate", propertyParserFactory.getParser(Datable.class));
    parsers.put("birthDate", propertyParserFactory.getParser(Datable.class));
    parsers.put("modified", propertyParserFactory.getParser(Change.class));
  }

  @Override
  public Set<String> getSortIndexPropertyNames() {
    Set<String> fieldList = new HashSet<>();
    for (String type : types) {
      for (String field : SORT_FIELDS) {
        fieldList.add(getSortPropertyName(type, field));
      }
    }
    return fieldList;
  }

  @Override
  public void addIndexedSortProperties(Vertex vertex) {
    for (String type : types) {
      for (String field : SORT_FIELDS) {
        Comparable<?> parsed = vertex.property(getPropertyName(type, field)).isPresent() ?
                parsers.get(field).parseForSort((String) vertex.property(getPropertyName(type, field)).value()) :
                parsers.get(field).parseForSort(null);


        if (parsed == null) {
          if (field.equals("names") && type.equals("wwperson") &&  vertex.property("wwperson_tempName").isPresent()) {
            Comparable<?> tempName = new TempNamePropertyParser().parseForSort(
                    (String) vertex.property("wwperson_tempName").value());
            vertex.property(getSortPropertyName(type, field), tempName);
          } else {
            vertex.property(getSortPropertyName(type, field), "");
          }
        } else {
          vertex.property(getSortPropertyName(type, field), parsed);
        }
      }
    }
  }

  private String getPropertyName(String type, String field) {
    if (field.equals("modified")) {
      return field;
    }
    return String.format("%s_%s", type, field);
  }

  private String getSortPropertyName(String type, String field) {
    if (field.equals("modified")) {
      return String.format("modified_%s", SORT_POSTFIX);
    }
    return String.format("%s_%s_%s", type, field, SORT_POSTFIX);
  }
}
