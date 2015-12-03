package nl.knaw.huygens.timbuctoo.vre;

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

import com.google.common.collect.Iterators;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.index.IndexCollection;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.search.RelationSearcher;
import nl.knaw.huygens.timbuctoo.search.converters.SearchResultConverter;

import java.util.Iterator;
import java.util.List;

public class WomenWritersVRE extends PackageVRE {
  private final List<String> receptions;

  public WomenWritersVRE(String vreId, String description, String modelPackage, List<String> receptions, Repository repository, RelationSearcher relationSearcher) {
    super(vreId, description, modelPackage, repository, relationSearcher);
    this.receptions = receptions;
  }

  WomenWritersVRE(String vreId, String description, Scope scopeMock, IndexCollection indexCollectionMock, SearchResultConverter resultConverterMock, Repository repositoryMock, RelationSearcher relationSearcher, List<String> receptions) {
    super(vreId, description, scopeMock, indexCollectionMock, resultConverterMock, repositoryMock, relationSearcher);
    this.receptions = receptions;
  }

  /**
   * Returns names of relation types that are considered to be receptions.
   */
  public List<String> getReceptionNames() {
    return receptions;
  }

  @Override
  protected Iterator<RelationType> filterRelationTypes(Iterator<RelationType> relationTypes) {

    return Iterators.filter(relationTypes, reception -> receptions.contains(reception.getRegularName()));
  }

  @Override
  public VREInfo toVREInfo() {
    VRE.VREInfo info = new VRE.VREInfo();
    info.setName(getVreId());
    info.setDescription(getDescription());

    for (String name : getReceptionNames()) {
      RelationType type = repository.getRelationTypeByName(name, false);
      if (type != null) {
        VRE.Reception reception = new VRE.Reception();
        reception.typeId = type.getId();
        reception.regularName = type.getRegularName();
        reception.inverseName = type.getInverseName();
        reception.baseSourceType = type.getSourceTypeName();
        reception.baseTargetType = type.getTargetTypeName();
        reception.derivedSourceType = mapTypeName(this, type.getSourceTypeName());
        reception.derivedTargetType = mapTypeName(this, type.getTargetTypeName());
        info.addReception(reception);
      }
    }
    return info;
  }

  private String mapTypeName(VRE vre, String iname) {
    return TypeNames.getInternalName(vre.mapTypeName(iname, true));
  }
}
