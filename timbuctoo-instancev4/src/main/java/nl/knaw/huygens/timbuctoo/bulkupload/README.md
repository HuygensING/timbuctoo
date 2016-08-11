## Context
Timbuctoo should allow users to upload datasets. 
We find that most of our users produce datasets in a tabular format (as opposed to a tree format or a graph format).
This package contains an code to parse streams of tabular data and to generate vertices to represent that data.

Originally this package also contained code to generate edges between the created vertices. 
This has been removed and replaced with an RML implementation.

## Responsibilies
This package is responsible for
 * Parsing various formats (currently excel only)
 * generating "collections" (i.e. tables, sheets, files) containing "rows" of "fields"
 * handling imports of arbitrary size (only limited by the space limitations of the target database)

It is currently not responsible for
 * converting the data to a specific data type (it should output plain text)
 * handling links (i.e. foreign keys or edges)
 * handling data that cannot be expressed as collections of rows

After an import is finished all traces of the original format should be gone.

## Data model
Given a dataset called `movie information` containing a table called `actors` with the following data:

first name | rating
------------ | -------------
Matt | 42
Saoirse | 44

you'd get the following graph

```
                                                                     ┌───────────hasFirstProperty────────┐          
                                                                     │                                   │          
                                                                     │                                   ▼          
                                                                     │                        ┌────────────────────┐
                                                                     │                        │   id: 0            │
                                                                     │           ┌───────────▶│ name: "first name" │
                                                                     │           │            │order: 0            │
                                      ┌──────────────────────────────┤      hasProperty       └────────────────────┘
                                      │            «Vre»             │           │                       │          
                                      │  name: "movie information"   │───────────┤                hasNextProperty   
                                      │                              │           │                       ▼          
                                      └──────────────────────────────┘      hasProperty       ┌────────────────────┐
                                                      │                          │            │     id: 1          │
                                              hasRawCollection                   └───────────▶│   name: "rating"   │
                                                      │                                       │  order: 1          │
                                                      ▼                                       └────────────────────┘
                                              ┌───────────────┐                                                     
        ┌────────────hasFirstItem─────────────│ name: actors  │                                                     
        │                                     └───────────────┘                                                     
        │                                             │                                                             
        │                 ┌────────hasItem────────────┴────────hasItem────┐                                         
        │                 │                                               │                                         
        │                 ▼                                               ▼                                         
        │  ┌─────────────────────────────┐                  ┌──────────────────────────┐                            
        │  │ first name_value: "Saoirse" │                  │ first name_value: "Matt" │                            
        └─▶│     rating_value: "44"      │────hasNextItem──▶│     rating_value: "42"   │                            
           └─────────────────────────────┘                  └──────────────────────────┘                            
```

Note the following:

 1. the _value postfix is needed because we will also store mapping errors next to these values. e.g. if a value is interpreted as foreign key, but it's target is not available then an error will be stored on the raw entity vertex under `<property>_error`
 2. the property descriptions are stored mostly to be able to traverse them in order.
 3. Together with the ordered traversal of the items (`hasNextItem`) this allows us to rerender the table as it was uploaded.
 4. All properties are stored as text, regardless of the datatypes that the source format supports.

### Contents of this package
 * [loaders](loaders) contains two variants of an Excel loader at the moment. To add a new loader you'd add an implementation of [BulkLoader](loaders/BulkLoader.java) here.
   * [AllSheetLoader](loaders/excel/allsheetloader/AllSheetLoader.java) loads all columns of all sheets. *This is the one that timbuctoo currently uses*.
   * [StyleAwareXlsxLoader](loaders/excel/styleawarexlsxloader/StyleAwareXlsxLoader.java) loads only the columns that are marked with a specially named style. *This is not used at the moment.*
 * [parsing state machine](parsingstatemachine) contains the actual import logic. 
 * [savers](savers) contains only a Tinkerpop saver at the moment.
   To load data in a different database you'd add your own implementation of [Saver](savers/Saver.java) here. 
   To change how the data is stored in the graph you'd change the code in [TinkerpopSaver](savers/TinkerpopSaver.java) 
  
