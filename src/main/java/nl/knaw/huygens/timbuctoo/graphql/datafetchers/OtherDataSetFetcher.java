package nl.knaw.huygens.timbuctoo.graphql.datafetchers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.DataSetWithDatabase;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectGraphReference;
import nl.knaw.huygens.timbuctoo.graphql.security.UserPermissionCheck;
import nl.knaw.huygens.timbuctoo.security.dto.User;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;

public class OtherDataSetFetcher implements DataFetcher<List<Map>> {
  private final DataSetRepository repo;

  public OtherDataSetFetcher(DataSetRepository repo) {
    this.repo = repo;
  }

  @Override
  public List<Map> get(DataFetchingEnvironment env) {
    if (env.getSource() instanceof SubjectGraphReference) {
      SubjectGraphReference source = env.getSource();
      Optional<User> optUser = env.getGraphQlContext().get("user");
      UserPermissionCheck userPermissionCheck = env.getGraphQlContext().get("userPermissionCheck");

      Stream<DataSet> dataSets = optUser
        .map(user -> repo.getDataSetsWithReadAccess(user).stream())
        .orElseGet(() -> repo.getDataSets().stream().filter(d -> d.getMetadata().isPublished()));

      if (env.containsArgument("dataSetIds")) {
        final Set<String> dataSetIds = new HashSet<>(env.getArgument("dataSetIds"));
        dataSets = dataSets.filter(d -> dataSetIds.contains(d.getMetadata().getCombinedId()));
      }
      final String ownId = source.getDataSet().getMetadata().getCombinedId();
      return dataSets
        .filter(d -> !ownId.equals(d.getMetadata().getCombinedId()))
        .map(d -> {
          try (Stream<CursorQuad> quads = d.getQuadStore().getQuadsInGraph(source.getSubjectUri(), source.getGraph())) {
            return tuple(d, quads.findAny());
          }
        })
        .filter(i -> i.right().isPresent())
        .map(i -> ImmutableMap.of(
          "metadata", new DataSetWithDatabase(i.left(), userPermissionCheck),
          "entity", new LazyTypeSubjectReference(i.right().get().getSubject(), source.getGraph(), i.left())
        ))
        .collect(toList());
    }
    return Lists.newArrayList();
  }
}
