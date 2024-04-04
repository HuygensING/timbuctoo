package nl.knaw.huygens.timbuctoo.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.graphql.defaultconfiguration.SummaryProp;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.util.RdfConstants.TIM_SUMMARYDESCRIPTIONPREDICATE;

public class EntityDescriptionFetcher extends EntityFetcher {
  public EntityDescriptionFetcher(List<SummaryProp> defaultDescriptions) {
    super(TIM_SUMMARYDESCRIPTIONPREDICATE, defaultDescriptions, false);
  }
}
