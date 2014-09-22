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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

/**
 * Creates a reception graph for visualization with d3.js.
 * Such a graph shows how persons react on (the work of) a person.
 * As such it summarizes two types of path in the data:
 * 1) work-author --> work --> reception --> reception-author
 * 2) work-author --> reception --> reception-author.
 */
public class ReceptionGraphBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(ReceptionGraphBuilder.class);

  private final Repository repository;
  private final Graph graph;

  public ReceptionGraphBuilder(Repository repository) {
    this.repository = repository;
    graph = new Graph();
  }

  public Graph getGraph() {
    return graph;
  }

  public void addPerson(VRE vre, Person person) throws StorageException {
    Stopwatch stopWatch = Stopwatch.createStarted();

    String personId = person.getId();
    //System.out.printf("personId: %s%n", personId);
    String isCreatedById = repository.getRelationTypeByName("isCreatedBy").getId();
    //System.out.printf("isCreatedById: %s%n", isCreatedById);
    List<String> receptionTypeIds = repository.getRelationTypeIdsByName(vre.getReceptionNames());
    //System.out.printf("receptionTypeIds: %s%n", receptionTypeIds);

    List<Relation> isCreatorOfRelations = repository.findRelations(null, personId, isCreatedById).getAll();
    // loop over all works
    for (Relation isCreatorOfRelation : isCreatorOfRelations) {
      String workId = isCreatorOfRelation.getSourceId();
      //System.out.printf("workId: %s%n", workId);
      // loop over all reception types
      for (String receptionTypeId : receptionTypeIds) {
        List<Relation> receptionRelations = repository.findRelations(workId, null, receptionTypeId).getAll();
        // loop over all receptions of this type on this work
        for (Relation receptionRelation : receptionRelations) {
          String receptionId = receptionRelation.getTargetId();
          //System.out.printf("receptionId: %s (type %s)%n", receptionId, receptionTypeId);
          List<Relation> receptionAuthorRelations = repository.findRelations(receptionId, null, isCreatedById).getAll();
          // loop over all authors of this reception
          // does it actually occur that there is more than one?
          for (Relation receptionAuthorRelation : receptionAuthorRelations) {
            //System.out.printf("sddong: %s%n", receptionAuthorRelation.getTargetId());
            graph.addWeightToEdge(personId, receptionAuthorRelation.getTargetId(), 1);
          }
        }
      }
    }
    LOG.info("Time for person {}: {}", personId, stopWatch);

    // TODO add immediate receptions on person
  }

}
