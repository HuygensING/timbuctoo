package nl.knaw.huygens.timbuctoo.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.graphql.defaultconfiguration.SummaryProp;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.util.RdfConstants.TIM_SUMMARYTITLEPREDICATE;

public class EntityTitleFetcher extends EntityFetcher {
  public EntityTitleFetcher(List<SummaryProp> defaultTitles) {
    super(TIM_SUMMARYTITLEPREDICATE, defaultTitles, true);
  }
}
