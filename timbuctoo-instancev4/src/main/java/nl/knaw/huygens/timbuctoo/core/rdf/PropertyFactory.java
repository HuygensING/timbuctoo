package nl.knaw.huygens.timbuctoo.core.rdf;

import nl.knaw.huygens.timbuctoo.core.RdfImportErrorReporter;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.CreateProperty;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.ImmutableCreateProperty;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.PredicateInUse;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.ValueTypeInUse;

import java.util.List;
import java.util.Stack;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public class PropertyFactory {
  private final RdfImportErrorReporter importErrorReporter;

  public PropertyFactory(RdfImportErrorReporter importErrorReporter) {
    this.importErrorReporter = importErrorReporter;
  }

  public List<CreateProperty> fromPredicates(List<PredicateInUse> predicates) {
    return predicates.stream().map(pred -> convertToProperty(pred)).collect(toList());
  }

  private CreateProperty convertToProperty(PredicateInUse pred) {
    Stack<ValueTypeInUse> valueTypes = pred.getValueTypes().stream()
                                           .sorted(comparingInt(o -> o.getEntitiesConnected().size()))
                                           .collect(toCollection(Stack::new));
    ValueTypeInUse type = valueTypes.pop();

    valueTypes.forEach(vt -> vt
      .getEntitiesConnected().forEach(e -> importErrorReporter
        .entityHasWrongTypeForProperty(e, pred.getPredicateUri(), type.getTypeUri(), vt.getTypeUri())
      )
    );

    return ImmutableCreateProperty.builder()
                                  .clientName(getPredicateName(pred))
                                  .rdfUri(pred.getPredicateUri())
                                  .typeUri(type.getTypeUri())
                                  .propertyType(getPropertyType(type))
                                  .build();
  }

  private String getPropertyType(ValueTypeInUse type) {
    // TODO move to factory and add support for other types
    return "string";
  }

  private String getPredicateName(PredicateInUse pred) {
    String[] split = pred.getPredicateUri().split("/");
    return split[split.length - 1];
  }
}
