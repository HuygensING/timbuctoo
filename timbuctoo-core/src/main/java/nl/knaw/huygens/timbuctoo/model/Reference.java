package nl.knaw.huygens.timbuctoo.model;

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

import static com.google.common.base.Preconditions.checkNotNull;
import nl.knaw.huygens.timbuctoo.config.TypeNames;

/**
 * A reference to an {@code Entity} instance,
 * allowing it to be retrieved from the storage.
 */
public class Reference {

  /** The internal type name. */
  private String type;
  /** The identifier. */
  private String id;

  // For deserialization...
  public Reference() {}

  public Reference(Class<? extends Entity> typeToken, String id) {
    checkNotNull(typeToken);
    this.type = TypeNames.getInternalName(typeToken);
    this.id = id;
  }

  public Reference(String type, String id) {
    this.type = type;
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Reference) {
      Reference that = (Reference) object;
      return (this.type == null ? that.type == null : this.type.equals(that.type)) //
          && (this.id == null ? that.id == null : this.id.equals(that.id));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (type == null ? 0 : type.hashCode());
    result = 31 * result + (id == null ? 0 : id.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return String.format("{%s,%s}", type, id);
  }

  /**
   * Checks if the reference refers to a type.
   * @param typeToCheck the type to check
   * @return {@code true} if it refers to the type, {@code false} if not.
   * @throws NullPointerException if {@code typeToCheck} is equal to null.
   */
  public boolean refersToType(Class<? extends Entity> typeToCheck) {
    checkNotNull(typeToCheck);
    return type.equals(TypeNames.getInternalName(typeToCheck));
  }

}
