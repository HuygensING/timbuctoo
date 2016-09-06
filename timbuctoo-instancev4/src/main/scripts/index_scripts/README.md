# Solr index script

This folder contains scripts that scrape timbuctoo and send the data to solr.

## Context
We want to be able expose Timbuctoo collections through solr/lucene in a flexible way,
enabling indexes on both the direct properties of records in a collection, as well as
key properties of directly related objects; for instance a keyword value from a keyword 
collection, which was linked to a document from a document collection (like genre).


### Usage
Creating an index for a Timbuctoo collection is a 3-step process:

1. Scrape Timbuctoo data
2. Convert to solr docs
3. Send solr docs to solr server

#### Libraries
For that purpose there are three classes in the dir ```lib/timbuctoo_solr```.

1. TimbuctooIO ```lib/timbuctoo_solr/timbuctoo_io.rb```
2. DefaultMapper ```lib/timbuctoo_solr/default_mapper.rb```
3. SolrIO ```lib/timbuctoo_solr/solr_io.rb```

The next sections will explain the 3-step process in more detail, using examples which 
can be found in the ```samples``` dir.

#### Scraping
A scrape of a Timbuctoo collection is done in batches using the method 
```scrape_collection``` in TimbuctooIO.

```ruby
    # samples/basic-scrape.rb
    require '../lib/timbuctoo_solr/timbuctoo_io'

    timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl')
    timbuctoo_io.scrape_collection('dcararchives')    
```




## Open issues

### 1. No control over text indexes
Our current solr index is the default data driven index. 
This one does not handle accented words very well. 
We also have no control over how capitalization is handled.
We're pretty sure that our users need us to handle these cases in a way that differs on a project by project basis.

#### Development steps
- Run a local solr 6 instance (docker or directly under windows, you'll need to be able to access the files)
- create the index `wwpersons_accent_research` on your local solr
- Fill this index with the fulltext records (i.e. `*_t`) uit wwpersons
- Configure it so that 
 1. a search for `Bronte` and a search fot `Brontë` both return one instance for each of the three Brontë sisters
 2. a search for `de*` return both "Descartes" and "Eugénie Avril de Sainte Croix". A Search for `De*` returns only Descartes
 3. Find out how we could make the index fully case-sensitive
- How can we make this approach work for all `*_t` fields?
- How can we make this approach work for only a specific field?
