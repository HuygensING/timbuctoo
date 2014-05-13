package nl.knaw.huygens.timbuctoo.vre;

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

import java.io.IOException;
import java.util.List;

public class CobwwwebNoVRE extends AbstractVRE {

  @Override
  protected Scope createScope() throws IOException {
    return new CobwwwebNoScope();
  }

  @Override
  public String getName() {
    return "CobwwwebNo";
  }

  @Override
  public String getDescription() {
    return "VRE for the 'COBWWWEB-Norway' subproject.";
  }

  @Override
  public String getDomainEntityPrefix() {
    return "cwno";
  }

  @Override
  public List<String> getReceptionNames() {
    return WomenWritersVRE.RECEPTION_NAMES;
  }

}
