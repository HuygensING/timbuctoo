package nl.knaw.huygens.timbuctoo.tools.conversion;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import java.util.Collection;

public class VerificationException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  private String oldId;
  private String newId;
  private Collection<Mismatch> mismatches;

  public VerificationException(String oldId, String newId, Collection<Mismatch> mismatches) {
    this.oldId = oldId;
    this.newId = newId;
    this.mismatches = mismatches;
  }

  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("Object with old id \"%s\" and new id \"%s\" did not match between databases.", oldId, newId));
    sb.append("\n");
    sb.append("The following properties did not match:\n");
    for (Mismatch mismatch : mismatches) {
      sb.append(mismatch);
      sb.append("\n");
    }
    return sb.toString();
  }
}
