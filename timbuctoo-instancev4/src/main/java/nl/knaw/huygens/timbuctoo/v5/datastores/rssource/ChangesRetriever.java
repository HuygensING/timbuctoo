package nl.knaw.huygens.timbuctoo.v5.datastores.rssource;

import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTruePatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.UpdatedPerPatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChangesRetriever {
  UpdatedPerPatchStore updatedPerPatchStore;
  BdbTruePatchStore bdbTruePatchStore;
  String graph; //pass in graph from the dataset for now as the QuadStore does not currently contain it.
  ChangesQuadGenerator changesQuadGenerator;

  public ChangesRetriever(UpdatedPerPatchStore updatedPerPatchStore, BdbTruePatchStore bdbTruePatchStore,
                          String graph) {
    this.updatedPerPatchStore = updatedPerPatchStore;
    this.bdbTruePatchStore = bdbTruePatchStore;
    this.graph = graph;
    this.changesQuadGenerator = new ChangesQuadGenerator(graph);
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

  public List<String> retrieveChanges(Integer version, Supplier<List<String>> subjects) {
    List<String> changes = new ArrayList<>();

    subjects.get().forEach(subject -> {
      try (Stream<CursorQuad> quads = bdbTruePatchStore.getChanges(subject, version, true)) {
        // Filtering on direction is needed as the TruePatchStore contains the inverse relations as well.
        // The original relations are always in the "OUT" direction.
        // See StoreUpdater.addRelation
        quads.filter(quad -> quad.getDirection().equals(Direction.OUT)).forEach(quad -> {
          Optional<String> dataType = quad.getValuetype();
          if (dataType == null || !dataType.isPresent()) {
            if (quad.getChangeType() == ChangeType.ASSERTED) {
              changes.add(
                changesQuadGenerator.onRelation(quad.getSubject(), quad.getPredicate(), quad.getObject(), graph)
              );
            } else if (quad.getChangeType() == ChangeType.RETRACTED) {
              changes.add(
                changesQuadGenerator.delRelation(quad.getSubject(), quad.getPredicate(), quad.getObject(), graph)
              );
            }
          } else {
            Optional<String> language = quad.getLanguage();
            if (language != null && language.isPresent() && dataType.equals(RdfConstants.LANGSTRING)) {
              if (quad.getChangeType() == ChangeType.ASSERTED) {
                changes.add(
                  changesQuadGenerator.onLanguageTaggedString(
                    quad.getSubject(),
                    quad.getPredicate(),
                    quad.getObject(),
                    language.get(),
                    graph
                  )
                );
              } else if (quad.getChangeType() == ChangeType.RETRACTED) {
                changes.add(
                  changesQuadGenerator.delLanguageTaggedString(
                    quad.getSubject(),
                    quad.getPredicate(),
                    quad.getObject(),
                    language.get(),
                    graph
                  )
                );
              } else {
                if (quad.getChangeType() == ChangeType.ASSERTED) {
                  changes.add(
                    changesQuadGenerator.onValue(
                      quad.getSubject(),
                      quad.getPredicate(),
                      quad.getObject(),
                      dataType.get(),
                      graph
                    )
                  );
                } else if (quad.getChangeType() == ChangeType.RETRACTED) {
                  changes.add(
                    changesQuadGenerator.delValue(
                      quad.getSubject(),
                      quad.getPredicate(),
                      quad.getObject(),
                      dataType.get(),
                      graph
                    )
                  );
                }
              }
            }
          }

        });
      }
    });

    return changes;
  }

}
/*
if (dataType == null || dataType.isEmpty()) {
  this.onRelation(subject, predicate, object, graph);
  } else {
  if (language != null && !language.isEmpty() && dataType.equals(RdfConstants.LANGSTRING)) {
  this.onLanguageTaggedString(subject, predicate, object, language, graph);
  } else {
  this.onValue(subject, predicate, object, dataType, graph);
  }
  }*/
