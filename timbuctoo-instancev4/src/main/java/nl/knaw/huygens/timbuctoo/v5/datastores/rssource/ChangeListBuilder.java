package nl.knaw.huygens.timbuctoo.v5.datastores.rssource;

import nl.knaw.huygens.timbuctoo.v5.dataset.ChangesRetriever;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChangeListBuilder {
  private final ChangesQuadGenerator changesQuadGenerator;

  public ChangeListBuilder() {
    this.changesQuadGenerator = new ChangesQuadGenerator();
  }

  public List<String> retrieveChangeFileNames(Supplier<List<Integer>> versions) {
    List<String> changeFileNames = new ArrayList<>();
    //filter hack to ignore isClosed values. needs to be fixed in DataRetriever.
    versions.get().stream().filter(x -> x != Integer.MAX_VALUE).collect(Collectors.toList())
      .forEach(version -> {
        changeFileNames.add("changes" + version.toString() + ".nqud");
      });

    return changeFileNames;
  }

  public Stream<String> retrieveChanges(ChangesRetriever changesRetriever, Integer version) {
    Stream<CursorQuad> quads = changesRetriever.retrieveChanges(version);
    return quads.map(quad -> {
      Optional<String> dataType = quad.getValuetype();
      if (dataType == null || !dataType.isPresent()) {
        if (quad.getChangeType() == ChangeType.ASSERTED) {
          return changesQuadGenerator.onRelation(quad.getSubject(), quad.getPredicate(),
              quad.getObject(), quad.getGraph().orElse(null));
        } else if (quad.getChangeType() == ChangeType.RETRACTED) {
          return changesQuadGenerator.delRelation(quad.getSubject(), quad.getPredicate(),
              quad.getObject(), quad.getGraph().orElse(null));
        }
      } else {
        Optional<String> language = quad.getLanguage();
        if (language != null && language.isPresent() && dataType.get().equals(RdfConstants.LANGSTRING)) {
          if (quad.getChangeType() == ChangeType.ASSERTED) {
            return changesQuadGenerator.onLanguageTaggedString(
              quad.getSubject(),
              quad.getPredicate(),
              quad.getObject(),
              language.get(),
              quad.getGraph().orElse(null)
            );
          } else if (quad.getChangeType() == ChangeType.RETRACTED) {
            return changesQuadGenerator.delLanguageTaggedString(
              quad.getSubject(),
              quad.getPredicate(),
              quad.getObject(),
              language.get(),
              quad.getGraph().orElse(null)
            );
          }
        } else {
          if (quad.getChangeType() == ChangeType.ASSERTED) {
            return changesQuadGenerator.onValue(
              quad.getSubject(),
              quad.getPredicate(),
              quad.getObject(),
              dataType.get(),
              quad.getGraph().orElse(null)
            );
          } else if (quad.getChangeType() == ChangeType.RETRACTED) {
            return changesQuadGenerator.delValue(
              quad.getSubject(),
              quad.getPredicate(),
              quad.getObject(),
              dataType.get(),
              quad.getGraph().orElse(null)
            );
          }
        }
      }
      return ""; // return empty string for unchanged quads
    });
  }
}
