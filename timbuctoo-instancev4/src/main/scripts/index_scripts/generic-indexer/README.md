# Generic indexer

These scripts are meant to create a _generic_ for a Timbuctoo collection index 
based on its archetype.

It is meant to be run on new imported Timbuctoo sets, which will provide a dynamically created vreId.


## Running from command line
The indexer can be started via the run.rb script (see source for expected parameters):

```ruby
   opts.on('-d', '--dump-dir DIR', 'Save dump of scraped Timbuctoo into a dir') 
   opts.on('-f', '--from-file', 'Scrape timbuctoo from local file cache') 
   opts.on('-t', '--timbuctoo-url TIM_URL', 'Base url for Timbuctoo') 
   opts.on('-s', '--solr-url SOLR_URL', 'Base url for Timbuctoo') 
   opts.on('-a', '--solr-auth AUTH', 'Value for Authentication header of solr server') 
   opts.on('-V', '--vre-id VRE', 'The VRE ID to scrape')
```

## Starting the web app
The indexer can also be started through the generic search webapp, when the correct environment variables are set.

```sh
$ export TIMBUCTOO_URL=http://localhost:8080
$ export SOLR_URL=http://localhost:8983/solr
$ export PORT=4567
$ foreman start
```

Then navigate to http://localhost:4567?vreId=TheIdOfYourVre

## Running the webapp with docker
There is also a Dockerfile wrapping the web-app in /src/main/scripts/index_scripts.


```sh
$ docker build -t huygensing/timbuctoo-generic-search .
$ docker run -p -e SOLR_URL='http://solr' -e TIMBUCTOO_URL='http://timbuctoo' 80:80 huygensing/timbuctoo-generic-search
```


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


## Javascript sources for client app are from this project:

https://github.com/HuygensING/timbuctoo-generic-search-client/tree/master
