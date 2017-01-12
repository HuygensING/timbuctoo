package nl.knaw.huygens.timbuctoo.rest.util;

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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.knaw.huygens.timbuctoo.search.RawSearchResult;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Singleton
public class AutocompleteResultConverter {
  private final AutocompleteResultEntryConverter entryConverter;

  @Inject
  public AutocompleteResultConverter(AutocompleteResultEntryConverter entryConverter) {
    this.entryConverter = entryConverter;
  }

  public RawSearchResult convert(RawSearchResult rawSearchResult, URI entityURI) {
    List<Map<String,Object>> convertedResult = Lists.newArrayList();
    // convert the results to what the client wants to see
    for(Map<String, Object> resultEntry : rawSearchResult.getResults()){
      convertedResult.add(entryConverter.convert(resultEntry, entityURI));
    }

    return new RawSearchResult(rawSearchResult.getTotal(), convertedResult);
  }


}
