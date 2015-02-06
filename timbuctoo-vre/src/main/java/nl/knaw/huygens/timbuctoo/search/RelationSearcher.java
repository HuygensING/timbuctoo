package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo VRE
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

import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

public abstract class RelationSearcher {

  protected final Repository repository;

  public RelationSearcher(Repository repository) {
    this.repository = repository;
  }

  public abstract SearchResult search(VRE vre, Class<? extends DomainEntity> relationType, RelationSearchParameters relationSearchParameters) throws SearchException, SearchValidationException;

  protected List<String> getRelationTypes(List<String> relationTypeIds, VRE vre) {
    if (relationTypeIds != null && !relationTypeIds.isEmpty()) {
      return relationTypeIds;
    }

    // TODO find a more generic way, to retrieve the relation ids of a VRE.
    return repository.getRelationTypeIdsByName(vre.getReceptionNames());
  }

}
