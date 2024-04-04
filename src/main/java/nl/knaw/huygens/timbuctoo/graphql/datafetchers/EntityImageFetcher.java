package nl.knaw.huygens.timbuctoo.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.graphql.defaultconfiguration.SummaryProp;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.util.RdfConstants.TIM_SUMMARYIMAGEPREDICATE;

public class EntityImageFetcher extends EntityFetcher {
  public EntityImageFetcher(List<SummaryProp> defaultImages) {
    super(TIM_SUMMARYIMAGEPREDICATE, defaultImages, false);
  }
}
