package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.GraphQlSchemaUpdater;

public abstract class Mutation<T> implements DataFetcher<T> {

  private final GraphQlSchemaUpdater schemaUpdater;

  public Mutation(GraphQlSchemaUpdater schemaUpdater) {

    this.schemaUpdater = schemaUpdater;
  }

  @Override
  public final T get(DataFetchingEnvironment environment) {
    T value = this.executeAction(environment);
    schemaUpdater.updateSchema();
    return value;
  }

  protected abstract T executeAction(DataFetchingEnvironment environment);

}
