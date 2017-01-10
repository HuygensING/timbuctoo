# Multi collection indexer

This indexer indexes all the timbuctoo datasets of a given timbuctoo instance into one index.

## Running the ruby-2.x script 

The scripts requires the httplog rubygem and ruby-2.2 or above.


```sh
ruby run.rb -t $TIMBUCTOO_SCRAPE_URL -s $SOLR_URL -a $SOLR_AUTH -i $INDEX_NAME
```

## Running the indexing script in docker

This script is also hosted on the timbuctoo docker hub.

```
docker run -e TIMBUCTOO_SCRAPE_URL=http://12.123.12.12:8080 -e SOLR_URL=http://12.123.12.12:8983/solr -e INDEX_NAME=indexname -t huygensing/timbuctoo:faceted-search-multi-collection-site
```

## The frontend 

More information on the frontend for this index with timbuctoo is in the github project [timbuctoo-multi-collection-search](https://github.com/huygensing/timbuctoo-multi-collection-search)
