package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.rest.util.Status;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import com.google.inject.Inject;

@Path(Paths.SYSTEM_PREFIX + "/status")
public class StatusResource {

  private final StorageManager storageManager;
  private final IndexManager indexManager;

  @Inject
  public StatusResource(StorageManager storageManager, IndexManager indexManager) {
    this.storageManager = storageManager;
    this.indexManager = indexManager;
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  @APIDesc("Returns the status of the webapp.")
  public Status getStatus() {
    Status status = new Status();
    status.setStorageStatus(storageManager.getStatus());
    status.setIndexStatus(indexManager.getStatus());
    return status;
  }

}
