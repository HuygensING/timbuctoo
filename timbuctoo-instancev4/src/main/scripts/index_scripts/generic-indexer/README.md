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
$ foreman start
```

Then navigate to http://localhost:4567