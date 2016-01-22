package nl.knaw.huygens.timbuctoo.server.search;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class PersonNames {
  public List<PersonName> list;

  public PersonNames() {
    list = Lists.newArrayList();
  }

  public PersonName defaultName() {
    return (list != null && !list.isEmpty()) ? list.get(0) : new PersonName();
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, false);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }
}
