# Generic indexer

## Dependencies

The run.rb script requires ruby 2.2 and upwards. 

The web app also depends on the ruby gem 'bundler' and ruby development
package
```sh
$ apt-get install ruby-dev # (may require root permissions)
$ gem install bundler # (may require root permissions)
```

## Installing the web app dependencies

```sh
$ bundle install # (may require root permissions)   
```

## Starting the web app

```sh
$ export TIMBUCTOO_URL=http://localhost:8080
$ export SOLR_URL=http://localhost:8983/solr
$ foreman start
```

Then navigate to http://localhost:4567

## Javascript sources for client app are from this project:

https://github.com/HuygensING/timbuctoo-generic-search-client/tree/master
