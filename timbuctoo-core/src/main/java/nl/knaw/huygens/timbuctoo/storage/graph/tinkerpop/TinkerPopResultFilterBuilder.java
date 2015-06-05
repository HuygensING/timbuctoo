package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQuery;

import com.tinkerpop.blueprints.Element;

public class TinkerPopResultFilterBuilder {

  private final PropertyBusinessRules businessRules;
  private final PipeFunctionFactory pipeFunctionFactory;

  public TinkerPopResultFilterBuilder() {
    this(new PropertyBusinessRules(), new PipeFunctionFactory());
  }

  public TinkerPopResultFilterBuilder(PropertyBusinessRules businessRules, PipeFunctionFactory pipeFunctionFactory) {
    this.businessRules = businessRules;
    this.pipeFunctionFactory = pipeFunctionFactory;
  }

  public <T extends Element> TinkerPopResultFilter<T> buildFor(TimbuctooQuery query) {
    TinkerPopResultFilter<T> resultFilter = new TinkerPopResultFilter<T>(pipeFunctionFactory, businessRules);

    query.addFilterOptionsToResultFilter(resultFilter);

    return resultFilter;
  }
}
