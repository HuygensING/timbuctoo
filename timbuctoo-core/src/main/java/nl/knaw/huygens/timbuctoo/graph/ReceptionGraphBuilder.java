package nl.knaw.huygens.timbuctoo.graph;

/*
 * #%L
 * Timbuctoo core
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

import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

/**
 * Creates a reception graph, showing how persons react on (the work of) a person.
 * As such it summarizes two types of path in the data:
 * 1) work-author --> work --> reception --> reception-author
 * 2) work-author --> reception --> reception-author.
 */
public class ReceptionGraphBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(ReceptionGraphBuilder.class);

  private final Repository repository;
  private final String isCreatedById;
  private final List<String> receptionTypeIds;
  private final Graph graph;

  public ReceptionGraphBuilder(Repository repository, VRE vre) {
    this.repository = repository;
    isCreatedById = repository.getRelationTypeByName("isCreatedBy", true).getId();
    receptionTypeIds = repository.getRelationTypeIdsByName(vre.getReceptionNames());
    graph = new Graph();
  }

  public Graph getGraph() {
    return graph;
  }

  public ReceptionGraphBuilder addPerson(String personId, boolean isSubject) throws StorageException {
    graph.addVertex(personId);

    Stopwatch stopwatch = Stopwatch.createStarted();
    addReceptionsOnPerson(personId, isSubject);
    LOG.info("Receptions on person: {}", stopwatch);

    stopwatch = Stopwatch.createStarted();
    addReceptionsOnWorks(personId, isSubject);
    LOG.info("Receptions on works: {}", stopwatch);

    return this;
  }

  private void addReceptionsOnPerson(String personId, boolean isSubject) throws StorageException {
    addReceptions(personId, personId);
  }

  private void addReceptionsOnWorks(String personId, boolean isSubject) throws StorageException {
    List<Relation> relations = repository.findRelations(null, personId, isCreatedById).getAll();
    for (Relation relation : relations) {
      addReceptions(personId, relation.getSourceId());
    }
  }

  private void addReceptions(String personId, String sourceId) throws StorageException {
    for (String receptionTypeId : receptionTypeIds) {
      List<Relation> receptions = repository.findRelations(sourceId, null, receptionTypeId).getAll();
      for (Relation reception : receptions) {
        String receptionId = reception.getTargetId();
        List<Relation> receptionAuthorRelations = repository.findRelations(receptionId, null, isCreatedById).getAll();
        for (Relation receptionAuthorRelation : receptionAuthorRelations) {
          graph.addWeightToEdge(personId, receptionAuthorRelation.getTargetId(), 1);
        }
      }
    }
  }

}
