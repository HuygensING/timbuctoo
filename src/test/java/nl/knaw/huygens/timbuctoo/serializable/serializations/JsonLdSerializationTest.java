package nl.knaw.huygens.timbuctoo.serializable.serializations;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class JsonLdSerializationTest {
  @Test
  public void performSerialization() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonLdSerialization cs = new JsonLdSerialization(out);

    cs.serialize(SourceData.simpleResult());

    assertThat(out.toString(), is("""
        {
          "data" : {
            "Persons" : {
              "items" : [ {
                "@id" : "http://example.com/1",
                "@type" : "http://example.com/Person",
                "a" : {
                  "type" : "http://www.w3.org/2001/XMLSchema#int",
                  "value" : "1"
                },
                "b" : {
                  "prevCursor" : "next",
                  "items" : [ {
                    "@id" : "http://example.com/11",
                    "@type" : "http://example.com/SubItem",
                    "c" : {
                      "type" : "http://www.w3.org/2001/XMLSchema#string",
                      "value" : "2"
                    },
                    "d" : {
                      "items" : [ {
                        "type" : "http://www.w3.org/2001/XMLSchema#string",
                        "value" : "3"
                      }, {
                        "type" : "http://www.w3.org/2001/XMLSchema#string",
                        "value" : "4"
                      }, null ]
                    }
                  }, {
                    "@id" : "http://example.com/12",
                    "@type" : "http://example.com/SubItem",
                    "c" : {
                      "type" : "http://www.w3.org/2001/XMLSchema#string",
                      "value" : "5"
                    },
                    "d" : {
                      "items" : [ {
                        "type" : "http://www.w3.org/2001/XMLSchema#string",
                        "value" : "6"
                      }, {
                        "type" : "http://www.w3.org/2001/XMLSchema#string",
                        "value" : "7"
                      } ]
                    }
                  } ]
                }
              }, {
                "@id" : "http://example.com/2",
                "@type" : "http://example.com/Person",
                "a" : {
                  "type" : "http://www.w3.org/2001/XMLSchema#string",
                  "value" : "8"
                },
                "b" : {
                  "items" : [ {
                    "@id" : "http://example.com/11",
                    "@type" : "http://example.com/SubItem",
                    "c" : {
                      "type" : "http://www.w3.org/2001/XMLSchema#string",
                      "value" : "9"
                    },
                    "d" : {
                      "items" : [ {
                        "type" : "http://www.w3.org/2001/XMLSchema#int",
                        "value" : "10"
                      }, {
                        "type" : "http://www.w3.org/2001/XMLSchema#string",
                        "value" : "11"
                      } ]
                    }
                  }, {
                    "@id" : "http://example.com/12",
                    "@type" : "http://example.com/SubItem",
                    "c" : {
                      "type" : "http://www.w3.org/2001/XMLSchema#string",
                      "value" : "12"
                    },
                    "d" : {
                      "items" : [ {
                        "type" : "http://www.w3.org/2001/XMLSchema#double",
                        "value" : "13.0"
                      }, {
                        "type" : "http://www.w3.org/2001/XMLSchema#string",
                        "value" : "14"
                      } ]
                    }
                  }, {
                    "@id" : "http://example.com/13",
                    "@type" : "http://example.com/SubItem",
                    "c" : {
                      "type" : "http://www.w3.org/2001/XMLSchema#string",
                      "value" : "15"
                    },
                    "d" : {
                      "items" : [ {
                        "type" : "http://www.w3.org/2001/XMLSchema#string",
                        "value" : "16"
                      }, {
                        "type" : "http://www.w3.org/2001/XMLSchema#string",
                        "value" : "17"
                      }, {
                        "type" : "http://www.w3.org/2001/XMLSchema#string",
                        "value" : "18"
                      } ]
                    }
                  } ]
                }
              }, {
                "@id" : "http://example.com/3",
                "@type" : "http://example.com/Person",
                "a" : {
                  "type" : "http://www.w3.org/2001/XMLSchema#string",
                  "value" : "19"
                },
                "b" : {
                  "@id" : "http://example.com/21",
                  "@type" : "http://example.com/OtherSubItem",
                  "e" : {
                    "type" : "http://www.w3.org/2001/XMLSchema#string",
                    "value" : "20"
                  },
                  "f" : {
                    "type" : "http://www.w3.org/2001/XMLSchema#string",
                    "value" : "21"
                  }
                }
              } ]
            }
          },
          "@context" : {
            "data" : {
              "@id" : "@graph",
              "@container" : "@index"
            },
            "value" : "@value",
            "type" : "@type",
            "a" : "http://example.org/b",
            "b" : {
              "@reverse" : "http://example.org/b"
            },
            "c" : "http://example.org/c",
            "d" : "http://example.org/d",
            "a" : "http://example.org/a",
            "b" : "http://example.org/b",
            "e" : "http://example.org/e",
            "f" : "http://example.org/f"
          }
        }"""));
  }
}
