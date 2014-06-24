package nl.knaw.huygens.timbuctoo.model.cwno;

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

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.Relation;

public class CWNORelation extends Relation {
  @Override
  @IndexAnnotation(fieldName = "dynamic_s_sourceId", isFaceted = true)
  public String getSourceId() {
    return super.getSourceId();
  }

  @Override
  @IndexAnnotation(fieldName = "dynamic_s_targetId", isFaceted = true)
  public String getTargetId() {
    return super.getTargetId();
  }

  @Override
  @IndexAnnotation(fieldName = "dynamic_s_typeId", isFaceted = true)
  public String getTypeId() {
    return super.getTypeId();
  }
}
