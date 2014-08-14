package nl.knaw.huygens.timbuctoo.vre;

/*
 * #%L
 * Timbuctoo vre
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

import java.util.List;

import nl.knaw.huygens.timbuctoo.index.IndexFactory;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Provides access to the active VRE's, currently a fixed list.
 */
@Singleton
public class VREs implements VRECollection {

  private static final List<VRE> VRE_LIST = ImmutableList.<VRE> of( //
    new AdminVRE(), //
    new BaseVRE(), //
    new CKCCVRE(), //
    new DutchCaribbeanVRE(), //
    new WomenWritersVRE(), //
    new TestVRE());

  @Inject
  public VREs(IndexFactory indexFactory) {
    for (VRE vre: VRE_LIST) {
      vre.initIndexes(indexFactory);
    }
  }

  public List<VRE> getVREs() {
    return VRE_LIST;
  }

}
