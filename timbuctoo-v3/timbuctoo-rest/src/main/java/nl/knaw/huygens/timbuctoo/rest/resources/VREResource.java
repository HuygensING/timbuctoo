package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
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

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.config.Paths.SYSTEM_PREFIX;
import static nl.knaw.huygens.timbuctoo.config.Paths.VERSION_PATH_OPTIONAL;

@Path(VERSION_PATH_OPTIONAL + SYSTEM_PREFIX + "/vres")
public class VREResource extends ResourceBase {

  private VRECollection vres;

  @Inject
  public VREResource(Repository repository, VRECollection vres) {
    super(repository, vres);
    this.vres = vres;

  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  @APIDesc("Lists the available VRE's.")
  public Set<String> getAvailableVREs() {
    Set<String> ids = Sets.newTreeSet();
    for (VRE vre : vres.getAll()) {
      ids.add(vre.getVreId());
    }
    return ids;
  }

  @GET
  @Path("/{id}")
  @Produces({ MediaType.APPLICATION_JSON })
  @APIDesc("Provides info about the specified VRE.")
  public VRE.VREInfo getVREInfo(@PathParam("id") String vreId) {
    VRE vre = super.getValidVRE(vreId);

    VRE.VREInfo info = vre.toVREInfo();
    return info;
  }

}
