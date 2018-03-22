package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYIMAGEPREDICATE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.timPredicate;

public class EntityImageFetcher implements DataFetcher<TypedValue> {
  @Override
  public TypedValue get(DataFetchingEnvironment env) {
    if (env.getSource() instanceof SubjectReference) {
      DataSet dataSet = ((SubjectReference) env.getSource()).getDataSet();

      Optional<CursorQuad> image = dataSet.getQuadStore().getQuads(((SubjectReference) env.getSource()).getSubjectUri(),
        timPredicate(TIM_SUMMARYIMAGEPREDICATE), Direction.OUT, "").findFirst();

      if (image.isPresent()) {
        return TypedValue.create(image.get().getObject(), RdfConstants.STRING, dataSet);
      }

    }
    return null;
  }
}
