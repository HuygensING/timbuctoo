Timbuctoo
=========

## What is Timbuctoo?
Timbuctoo is developed by Huygens ING. A part of it's activities is research based around historical events. 
Timbuctoo solves two problems. The first one is to provide an infrastructure where multiple research projects can store their data. The second is that it allows multiple projects to have different opinions about the stored data. So overlapping data can easily be shared between multiple projects.

## How does it work?
Timbuctoo is a Java REST-service, that uses the JAX-RS implementation Jersey. It uses a Mongo database to store the data. It uses Solr for faster search results. It supports real persistency by using a [Handle service](http://handle.net/). The whole system is tested using Apache Tomcat.

## This guide
This guide is still a work in progress. Now it should contain the bare minimum to get a software developer started with the application.

## Getting Started

### Pre conditions
This guide expects that Mongo and a web server (i.e. Apache Tomcat) are installed. For compilation you need a Java 6 compiler and a Maven installed, that you can use in your command line.

#### Installation

Installation manuals set on January 20th 2014.

##### Red Hat, Fedora, CentOS

###### Mongodb
1. Create a yum repository '/etc/yum.repos.d/mongodb.repo', containing the following:

```
[mongodb]
name=MongoDB Repository
baseurl=http://downloads-distro.mongodb.org/repo/redhat/os/x86_64/
gpgcheck=0
enabled=1
```

For a 32 bit system it should look like the this:
```
[mongodb]
name=MongoDB Repository
baseurl=http://downloads-distro.mongodb.org/repo/redhat/os/i686/
gpgcheck=0
enabled=1
```

2. Then execute the following command to install mongo:`yum install mongo-10gen mongo-10gen-server`

Source: http://docs.mongodb.org/manual/tutorial/install-mongodb-on-red-hat-centos-or-fedora-linux/

###### Tomcat installation

1. Install the package: `yum install tomcat7`

##### Ubuntu

###### Mongo installation
1. Import the PGP key: `sudo apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10`

2. Create the sources list: `echo 'deb http://downloads-distro.mongodb.org/repo/debian-sysvinit dist 10gen' | sudo tee /etc/apt/sources.list.d/mongodb.list`

3. Reload the package database: `sudo apt-get update`

4. Install the packages: `sudo apt-get install mongodb-10gen`

Source: http://docs.mongodb.org/manual/tutorial/install-mongodb-on-ubuntu/

###### Tomcat installation
1. Install the package: `sudo apt-get install tomcat7`

For further information see: https://www.digitalocean.com/community/articles/how-to-install-apache-tomcat-on-ubuntu-12-04

##### Mac
1. Install homebrew: `ruby -e "$(curl -fsSL https://raw.github.com/mxcl/homebrew/go)"`

2. Update the homebrew packages: `brew update`

Source: http://brew.sh/

###### Mongo installation
1. Install mongo: `brew install mongo`

Source: http://docs.mongodb.org/manual/tutorial/install-mongodb-on-os-x/

###### Tomcat installation
1. Install with homebrew: `brew install tomcat`

Source: http://railscoder.com/installing-tomcat-with-homebrew-on-osx/

#### Example configuration
You have to change one thing in the example configuration (`timbuctoo-war-config-example`) in order to make your web application work. That is the database section of `config.xml`:

##### Mongo
```xml
<database>
  <!-- Mongo database -->
  <host>{server}</host>
  <name>{db name}</name> <!-- Could be any name if none is created -->
  <port>{port}</port> <!-- default mongo port is 27017 -->
  <user>{user name}</user>
  <password>{password}</password>
</database>
```


##### Solr
You also have to change the `{dir}` in the solr configuration. If the `use_user_home` is set to `true` the directory will be searched in the home directory of the user.
```xml
<solr>
  <use_user_home>true</use_user_home> <!-- does solr use the user home dir -->
  <directory>{dir}</directory> <!-- folder to find solr cores in. -->
  <!-- Maximum time before a commit in ms -->
  <commit_within>10000</commit_within>
</solr>
```
    
### Compilation
Go to the root of your git repository and execute the `mvn clean package -P example`. This will generate a war-file in the `timbuctoo-rest/target` Rename this file to `timbuctoo.war` and deploy it on your favourite web server like Apache Tomcat 6 or higher.

#### Deploy the software

When the project is build the module `timbuctoo-solr` contains a *zip-file (tar.gz)* in the target-folder. Extract that file to the directory you named in during the solr configuration and rename the newly created directory to `timbuctoo-solr`. You can do this with `tar -xvf {path to tar.gz} {target path}`

Now you can deploy Timbuctoo to Tomcat. The easiest way is to copy the `war` of the `timbuctoo-rest` module to the `webapps`-directory of Tomcat and rename it to `timbuctoo.war`.

### Demo pages
Timbuctoo has a few pages made for demo purposes. The pages are found in the `webapp/static` folder in the `timbuctoo-rest` Maven module. `index.html` shows the content of the database and the different solr indexes. `search.html` demonstrates a simple text  search page. Each `VRE` can search its own objects. Like the `VRE` *DWC* can search on each type defined in the Java package `nl.knaw.huygens.timbuctoo.model.dwcbia`. The same is true for *DutchCarribean*. The *Base* VRE will can search in the `model` root package. These model packages can be found in the `timbuctoo-core` Maven module.

Another interesting page is `user_info.html` in the `example_vre` subfolder  this demonstrates how to request a secured resource. This page can be reached on `{base_url}/timbuctoo/static/example_vre/user_info.html`.

### Supported REST calls
The demo pages show a small amount of REST resources availlable in Timbuctoo. Each non abstract class in the in model packages has it's own resource. The next example will show the available REST calls of the resource for the model class `nl.knaw.huygens.timbuctoo.model.dcar.DCARArchive`. 


* `{base_url}/timbuctoo/domain/dcararchives/ [GET]` shows the first 200 items of the collections
* `{base_url}/timbuctoo/domain/dcararchives/ [POST]` creates a new archive
* `{base_url}/timbuctoo/domain/dcararchives/ARCH000000001838 [GET]` shows a single item
* `{base_url}/timbuctoo/domain/dcararchives/ARCH000000001838 [PUT]` updates a single item
* `{base_url}/timbuctoo/domain/dcararchives/ARCH000000001838 [DELETE]` removes a single item

The resource name of a model can be determined as the lowercase name of the class postfixed with an s.

### Creating test data
In the `nl.knaw.huygens.timbuctoo.tools.util` package of the `timbuctoo-tools` module the `PrototypeCreator` gives an overview of how Timbuctoo expects the json be formatted. 
