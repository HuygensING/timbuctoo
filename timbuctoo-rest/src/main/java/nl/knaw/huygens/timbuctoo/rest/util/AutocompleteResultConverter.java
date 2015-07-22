package nl.knaw.huygens.timbuctoo.rest.util;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Singleton
public class AutocompleteResultConverter {
  private final AutocompleteResultEntryConverter entryConverter;

  public AutocompleteResultConverter(AutocompleteResultEntryConverter entryConverter) {
    this.entryConverter = entryConverter;
  }

  public Iterable<Map<String, Object>> convert(Iterable<Map<String, Object>> rawSearchResult, URI entityURI) {
    List<Map<String,Object>> convertedResult = Lists.newArrayList();
    for(Map<String, Object> resultEntry : rawSearchResult){
      convertedResult.add(entryConverter.convert(resultEntry, entityURI));
    }

    return convertedResult;
  }


}
