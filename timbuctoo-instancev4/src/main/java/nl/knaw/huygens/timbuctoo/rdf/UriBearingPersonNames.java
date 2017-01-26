package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.PersonName;

import java.util.List;
import java.util.Map;

// FIXME: add uri property to PersonName
// According to René it is blocked by a failing (de)serialization in the JsonCrudService at this moment.
public final class UriBearingPersonNames {
  public List<PersonName> list;
  // a map that contains the uri strings of of the PersonNames in list. The value corresponds with the position of
  // the PersonName in the list.
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
