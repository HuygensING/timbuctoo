package nl.knaw.huygens.timbuctoo.graph;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class D3Node {

  /** Unique key for this node. */
  private final String key;
  private final String type;
  private final String label;

  public D3Node(String key, String type, String label) {
    this.key = Preconditions.checkNotNull(key);
    this.type = Preconditions.checkNotNull(type);
    this.label = Strings.isNullOrEmpty(label) ? "-" : label;
  }

  public String getkey() {
    return key;
  }

  public String getType() {
    return type;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof D3Node) {
      return Objects.equal(key, ((D3Node) obj).key);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

}
