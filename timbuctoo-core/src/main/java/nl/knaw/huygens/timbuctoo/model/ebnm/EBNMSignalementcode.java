package nl.knaw.huygens.timbuctoo.model.ebnm;

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

import nl.knaw.huygens.timbuctoo.model.Signalementcode;

public class EBNMSignalementcode extends Signalementcode {

  private String signalementId;
  private String signalement;

  public String getLabel() {
    return getValue();
  }

  public String getCodeId() {
    return signalementId;
  }

  public void setCodeId(String _id) {
    this.signalementId = _id;
  }

  public String getSignalement() {
    return signalement;
  }

  public void setSignalement(String signalement) {
    this.signalement = signalement;
  }

}
