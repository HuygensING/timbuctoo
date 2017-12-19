package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.execution.ExecutionId;
import graphql.execution.ExecutionTypeInfo;
import graphql.language.Field;
import graphql.language.FragmentDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.BasicDataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.IllegalDataSetNameException;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTruePatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.StoreUpdater;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.UpdatedPerPatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.VersionStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.rmldatasource.RmlDataSourceStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.rml.RdfDataSourceFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class LookUpSubjectByUriFetcherWrapperTest {

  @Test
  public void handlesAbsoluteUrls() {
    LookUpSubjectByUriFetcherMock lookupFetcherMock = new LookUpSubjectByUriFetcherMock();
    LookUpSubjectByUriFetcherWrapper
      sut = new LookUpSubjectByUriFetcherWrapper("uri", lookupFetcherMock);

    sut.get(new MockEnv("http://example.com/2"));

    assertThat(lookupFetcherMock.uri, is("http://example.com/2"));
  }


  @Test
  public void handlesRelativeUrls() {
    LookUpSubjectByUriFetcherMock lookupFetcherMock = new LookUpSubjectByUriFetcherMock();
    LookUpSubjectByUriFetcherWrapper
      sut = new LookUpSubjectByUriFetcherWrapper("uri", lookupFetcherMock);
    sut.get(new MockEnv("/2"));

    assertThat(lookupFetcherMock.uri, is("http://example.org/2"));
  }

  @Test
  public void handlesEmptyUrls() {
    LookUpSubjectByUriFetcherMock lookupFetcherMock = new LookUpSubjectByUriFetcherMock();
    LookUpSubjectByUriFetcherWrapper
      sut = new LookUpSubjectByUriFetcherWrapper("uri", lookupFetcherMock);

    sut.get(new MockEnv(""));

    assertThat(lookupFetcherMock.uri, is("http://example.org"));
  }

  @Test
  public void doesntDoTooMuchNormalization() {
    LookUpSubjectByUriFetcherMock lookupFetcherMock = new LookUpSubjectByUriFetcherMock();
    LookUpSubjectByUriFetcherWrapper
      sut = new LookUpSubjectByUriFetcherWrapper("uri", lookupFetcherMock);

    sut.get(new MockEnv("."));

    assertThat(lookupFetcherMock.uri, is("http://example.org/"));
  }


  private class LookUpSubjectByUriFetcherMock implements LookUpSubjectByUriFetcher {
    private String uri;

    @Override
    public SubjectReference getItem(String uri, DataSet dataSet) {
      this.uri = uri;
      return null;
    }
  }

  private class MockEnv implements DataFetchingEnvironment {

    Map<String, Object> arguments = new HashMap<>();

    public MockEnv(String uri) {
      arguments.put("uri", uri);
    }

    @Override
    public DatabaseResult getSource() {
      return new DatabaseResult() {
        @Override
        public DataSet getDataSet() {
          return new DataSet() {
            @Override
            public SchemaStore getSchemaStore() {
              throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
            }

            @Override
            public TypeNameStore getTypeNameStore() {
              throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
            }

            @Override
            public ImportManager getImportManager() {
              throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
            }

            @Override
            public RdfDataSourceFactory getDataSource() {
              throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
            }

            @Override
            public QuadStore getQuadStore() {
              throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
            }

            @Override
            public DataSetMetaData getMetadata() {
              try {
                return new BasicDataSetMetaData(
                  "ownerid",
                  "datasetid",
                  "http://example.org",
                  "http://example.org/prefix/", false,false
                );
              } catch (IllegalDataSetNameException e) {
                throw new RuntimeException(e);
              }
            }

            @Override
            public void stop() {
            }

            @Override
            protected VersionStore getVersionStore() {
              throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
            }

            @Override
            protected BdbTruePatchStore getTruePatchStore() {
              throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
            }

            @Override
            protected UpdatedPerPatchStore getUpdatePerPatchStore() {
              throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
            }

            @Override
            protected RmlDataSourceStore getRmlDataSourceStore() {
              throw new UnsupportedOperationException("Not yet implemented");//FIXME: implement
            }
          };
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
    public <T> T getContext() {
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
    public GraphQLOutputType getFieldType() {
      throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public ExecutionTypeInfo getFieldTypeInfo() {
      throw new UnsupportedOperationException("");//FIXME: implement
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
  }
}
