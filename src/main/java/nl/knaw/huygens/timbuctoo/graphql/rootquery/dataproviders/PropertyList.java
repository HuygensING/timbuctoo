package nl.knaw.huygens.timbuctoo.graphql.rootquery.dataproviders;

import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface PropertyList {

  Optional<String> getPrevCursor();

  Optional<String> getNextCursor();

  List<Property> getItems();


}
