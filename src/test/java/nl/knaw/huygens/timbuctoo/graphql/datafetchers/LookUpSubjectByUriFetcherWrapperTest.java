package nl.knaw.huygens.timbuctoo.graphql.datafetchers;

import graphql.GraphQLContext;
import graphql.execution.ExecutionId;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.MergedField;
import graphql.execution.directives.QueryDirectives;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.FragmentDefinition;
import graphql.language.OperationDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import nl.knaw.huygens.timbuctoo.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.dataset.ReadOnlyChecker;
import nl.knaw.huygens.timbuctoo.dataset.dto.BasicDataSetMetaData;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.IllegalDataSetNameException;
import nl.knaw.huygens.timbuctoo.datastorage.DataSetStorage;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.BdbRmlDataSourceStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.BdbPatchVersionStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.DefaultResourcesStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.GraphStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.OldSubjectTypesStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.UpdatedPerPatchStore;
import nl.knaw.huygens.timbuctoo.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.dataset.CurrentStateRetriever;
import nl.knaw.huygens.timbuctoo.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.filestorage.FileStorage;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.util.Graph;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class LookUpSubjectByUriFetcherWrapperTest {
  @Test
  public void handlesAbsoluteUrls() {
    LookUpSubjectByUriFetcherMock lookupFetcherMock = new LookUpSubjectByUriFetcherMock();
    LookUpSubjectByUriFetcherWrapper sut = new LookUpSubjectByUriFetcherWrapper(lookupFetcherMock);

    sut.get(new MockEnv("http://example.com/2"));

    assertThat(lookupFetcherMock.uri, is("http://example.com/2"));
  }

  @Test
  public void handlesRelativeUrls() {
    LookUpSubjectByUriFetcherMock lookupFetcherMock = new LookUpSubjectByUriFetcherMock();
    LookUpSubjectByUriFetcherWrapper sut = new LookUpSubjectByUriFetcherWrapper(lookupFetcherMock);
    sut.get(new MockEnv("/2"));

    assertThat(lookupFetcherMock.uri, is("http://example.org/2"));
  }

  @Test
  public void handlesEmptyUrls() {
    LookUpSubjectByUriFetcherMock lookupFetcherMock = new LookUpSubjectByUriFetcherMock();
    LookUpSubjectByUriFetcherWrapper sut = new LookUpSubjectByUriFetcherWrapper(lookupFetcherMock);

    sut.get(new MockEnv(""));

    assertThat(lookupFetcherMock.uri, is("http://example.org"));
  }

  @Test
  public void doesntDoTooMuchNormalization() {
    LookUpSubjectByUriFetcherMock lookupFetcherMock = new LookUpSubjectByUriFetcherMock();
    LookUpSubjectByUriFetcherWrapper sut = new LookUpSubjectByUriFetcherWrapper(lookupFetcherMock);

    sut.get(new MockEnv("."));

    assertThat(lookupFetcherMock.uri, is("http://example.org/"));
  }

  private static class LookUpSubjectByUriFetcherMock implements LookUpSubjectByUriFetcher {
    private String uri;
    private Optional<Graph> graph;

    @Override
    public SubjectReference getItem(String uri, DataSet dataSet) {
      this.uri = uri;
      this.graph = Optional.empty();
      return null;
    }

    @Override
    public SubjectReference getItemInGraph(String uri, Optional<Graph> graph, DataSet dataSet) {
      this.uri = uri;
      this.graph = graph;
      return null;
    }
  }

  private static class MockEnv implements DataFetchingEnvironment {
    Map<String, Object> arguments = new HashMap<>();

    public MockEnv(String uri) {
      arguments.put("uri", uri);
    }

    @Override
    public DatabaseResult getSource() {
      return () -> new DataSet() {
        @Override
        public SchemaStore getSchemaStore() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        public UpdatedPerPatchStore getUpdatedPerPatchStore() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        public BdbPatchVersionStore getPatchVersionStore() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        public OldSubjectTypesStore getOldSubjectTypesStore() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        public TypeNameStore getTypeNameStore() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        public DefaultResourcesStore getDefaultResourcesStore() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        public QuadStore getQuadStore() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        public GraphStore getGraphStore() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        public BdbRmlDataSourceStore getRmlDataSourceStore() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        public ImportManager getImportManager() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        public DataSetMetaData getMetadata() {
          try {
            return new BasicDataSetMetaData(
                "ownerid",
                "datasetid",
                "http://example.org",
                "http://example.org/prefix/", false, false
            );
          } catch (IllegalDataSetNameException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public CurrentStateRetriever getCurrentStateRetriever() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        public ReadOnlyChecker getReadOnlyChecker() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        public DataSetStorage getDataSetStorage() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        public FileStorage getFileStorage() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        public void stop() {
        }

        @Override
        protected String getOwnerId() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        protected String getDataSetName() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }

        @Override
        protected BdbEnvironmentCreator getBdbEnvironmentCreator() {
          throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
        }
      };
    }

    @Override
    public Map<String, Object> getArguments() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public boolean containsArgument(String name) {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public <T> T getArgument(String name) {
      return (T) arguments.get(name);
    }

    @Override
    public <T> T getArgumentOrDefault(String name, T type) {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public <T> T getContext() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public GraphQLContext getGraphQlContext() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public <T> T getLocalContext() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public <T> T getRoot() {
      throw new UnsupportedOperationException("");//FIXME: implement
    }

    @Override
    public GraphQLFieldDefinition getFieldDefinition() {
      throw new UnsupportedOperationException("");//FIXME: implement
    }

    @Override
    public List<Field> getFields() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public MergedField getMergedField() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public Field getField() {
      throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
    }

    @Override
    public GraphQLOutputType getFieldType() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public ExecutionStepInfo getExecutionStepInfo() {
      throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
    }

    @Override
    public GraphQLType getParentType() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public GraphQLSchema getGraphQLSchema() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public Map<String, FragmentDefinition> getFragmentsByName() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public ExecutionId getExecutionId() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public DataFetchingFieldSelectionSet getSelectionSet() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public QueryDirectives getQueryDirectives() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public <K, V> DataLoader<K, V> getDataLoader(String dataLoaderName) {
      throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
    }

    @Override
    public DataLoaderRegistry getDataLoaderRegistry() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public Locale getLocale() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public OperationDefinition getOperationDefinition() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public Document getDocument() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public Map<String, Object> getVariables() {
      throw new IllegalStateException("Not implemented yet");
    }
  }
}
