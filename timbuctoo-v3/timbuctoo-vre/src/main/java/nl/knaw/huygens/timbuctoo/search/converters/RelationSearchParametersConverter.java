package nl.knaw.huygens.timbuctoo.search.converters;

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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetParameter;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetField;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParametersV2_1;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.model.Relation.TYPE_ID_FACET_NAME;

public class RelationSearchParametersConverter {

  public static final String RELATION_FACET = "dynamic_s_relation";
  private final Repository repository;
  public static final Predicate<FacetParameter> IS_RELATION_FACET = new Predicate<FacetParameter>() {
    @Override
    public boolean test(FacetParameter facetParameter) {
      return RELATION_FACET.equals(facetParameter.getName());
    }
  };
  public static final Logger LOG = LoggerFactory.getLogger(RelationSearchParametersConverter.class);

  @Inject
  public RelationSearchParametersConverter(Repository repository) {
    this.repository = repository;
  }

  public SearchParametersV1 toSearchParametersV1(RelationSearchParameters relationSearchParameters) {
    FacetParameter parameter = new DefaultFacetParameter(TYPE_ID_FACET_NAME, relationSearchParameters.getRelationTypeIds());
    ArrayList<FacetField> facetFields = Lists.newArrayList(new FacetField(TYPE_ID_FACET_NAME));

    return new SearchParametersV1().setFacetParameters(Lists.newArrayList(parameter)).setFacetFields(facetFields);
  }

  public RelationSearchParameters fromRelationParametersV2_1(Class<? extends Relation> relationType, RelationSearchParametersV2_1 parametersV2_1, VRE vre, Class<? extends DomainEntity> targetSearchType) throws SearchConversionException {
    try {
      RelationSearchParameters relationSearchParameters = new RelationSearchParameters();

      relationSearchParameters.setRelationTypeIds(getRelationTypeIds(parametersV2_1));
      relationSearchParameters.setSourceSearchId(parametersV2_1.getOtherSearchId());
      relationSearchParameters.setTargetSearchId(getTargetSearchId(parametersV2_1, vre, targetSearchType));
      relationSearchParameters.setTypeString(TypeNames.getInternalName(relationType));

      return relationSearchParameters;
    } catch (SearchException | SearchValidationException | StorageException | ValidationException e) {
      LOG.error("Exception while converting.", e);
      throw new SearchConversionException(e);
    }
  }

  private String getTargetSearchId(RelationSearchParametersV2_1 parametersV2_1, VRE vre, Class<? extends DomainEntity> targetSearchType) throws SearchException, SearchValidationException, ValidationException, StorageException {
    parametersV2_1.getFacetValues().removeIf(IS_RELATION_FACET);
    SearchResult searchResult = vre.search(targetSearchType, parametersV2_1);

    return repository.addSystemEntity(SearchResult.class, searchResult);
  }


  private List<String> getRelationTypeIds(RelationSearchParametersV2_1 parametersV2_1) {
    List<String> facetValues = parametersV2_1.getFacetValues().stream() //
      .filter(IS_RELATION_FACET) //
      .map(facetParameter1 -> ((DefaultFacetParameter) facetParameter1).getValues()) //
      .flatMap(values -> values.stream()).collect(Collectors.toList());
    return repository.getRelationTypeIdsByName(facetValues);
  }
}
