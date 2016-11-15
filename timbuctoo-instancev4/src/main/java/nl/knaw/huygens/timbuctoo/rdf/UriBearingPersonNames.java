package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.PersonName;

import java.util.List;
import java.util.Map;

public final class UriBearingPersonNames {
  public List<PersonName> list;
  public Map<String, Integer> nameUris;

  public UriBearingPersonNames() {
    list = Lists.newArrayList();
    nameUris = Maps.newHashMap();
  }

  @Override
  public String toString() {
    return "UriBearingPersonNames{" +
      "list=" + list +
      ", nameUris=" + nameUris +
      '}';
  }
}
