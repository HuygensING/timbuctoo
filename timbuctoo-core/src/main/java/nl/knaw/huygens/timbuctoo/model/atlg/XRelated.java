package nl.knaw.huygens.timbuctoo.model.atlg;

import org.apache.commons.lang.StringUtils;

// TODO Should eventually be refactored when the relations between objects are rethought.
public class XRelated {

  public String type;
  public String[] ids;

  @Override
  public String toString() {
    return String.format("  %s: %s", type, StringUtils.join(ids, " ##"));
  }

}
