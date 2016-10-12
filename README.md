# Timbuctoo

[![Join the chat at https://gitter.im/HuygensING/timbuctoo](https://badges.gitter.im/HuygensING/timbuctoo.svg)](https://gitter.im/HuygensING/timbuctoo?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

The code is available from https://github.com/HuygensING/timbuctoo

---

**Note:** This software is developed by the [Huygens Institute](http://huygens.knaw.nl) in the Netherlands. 
While we're currently intending to support the software indefinitely, we're certain that we'll support the software at least until *the end of 2018*.
This notice will be updated before then with the new support duration.

 * Support means that we'll review your pull requests and answer issues or questions on the chat.
 * It does not mean that we'll fix any issue that you raise.
 * But it does mean that we'll commit regularly with new features or patches (usually every workday).
 * While we try to make timbuctoo run everywhere, we're actively running it on Redhat 6. Using it on other platforms may trigger bugs that we are not aware of.

---

## Introduction

Timbuctoo is aimed at historians doing interpretative research.
Such a researcher collects facts from various documents, interprets and structures them, and creates tables of these facts. 
The researcher then uses this new dataset either as an object to do analysis on, or as an index that allows them to look at their sources from a distance quickly stepping back to the original source for the real interpretative work.

As such an historian you often need to categorize your findings. 
For example: you keep track of birthplaces.
You then need to decide how to write down the birthplace
 - Do you use the name of the city, or the burrough? 
 - Do you use the current name or the name when the person was born?
 - If your dataset spans a long time you might have two different cities on the same geographical location. Are they one name or more?

These judgements are sometimes the core of your research and sometimes incidental to it.
Timbuctoo aims to make it easier to publish your research dataset and then to re-use other people's published datasets.
To continue the example: another researcher might create a dataset containing locations, their coördinates and names and how that changed over time. You can then re-use that dataset and link to the entries instead of typing a string of characters that a humand might correctly interpret or not.

[![video](https://i.vimeocdn.com/video/596033818_640.webp)](https://vimeo.com/186090384)

## Related work
There are many tools for storing data (tabular or not). 
However, there are not many tools that will 
 - store the data
 - expose it in a way that it can be retrieved by another researcher
 - allow the researcher to base it's new dataset on that existing dataset (with a provenance trail)
 - keep track of updates to the source dataset and allow the user to subscribe to these changes.

Our current aim for timbuctoo is to fulfill these four criteria. Some things that timbuctoo will not do:

 - present the data in a faceted search (for this we're building a different tool that subscribes to timbuctoo's data)
 - clean up datasets (for this we point you to [openrefine](http://openrefine.org) and [karma](https://github.com/usc-isi-i2/Web-Karma))
 - visualize the data (for this we'll expose the data in timbuctoo in such a way that it can be picked up by tools such as [palladio](http://hdlab.stanford.edu/projects/palladio/) or [gephi](https://gephi.org/))

We will offer a basic editing interface but we'll also actively try to allow the data to be edited in external tools such as google spreadsheets, openrefine or excel.

## Getting up and running

### Sneak peek

To get a quick look at timbuctoo go to: https://repository.huygens.knaw.nl

### 1. System requirements

//TODO

### 2. Building it

This tool has been verified to work on Redhat 6.
To build your own version, you can either have 'java 8' and 'maven 3' installed or use the maven3 docker container.

* **build** using `mvn clean package`

### 3. Running it

 * Do a quick **debug run** using `./timbuctoo-instancev4/debugrun.sh` or  `docker-compose up`.
 If you do not use docker the external services, such as solr won't be started. 
 Timbuctoo will still run and the API will run fine, but some urls in the web GUI won't work.

to run a real version you'd run all services mentioned in the docker-compose.yml file yourself and start timbuctoo using

 * Run the timbuctoo service itself using `./timbuctoo-instancev4/target/appassembler/bin/timbuctoo server <your config>.yaml`. 
   You can use example_config.yaml for inspiration.
   To get search capabilities you also need to run the timbuctoo-query-gui and a solr instance.

 1. After launching it you should be greated by a login page.
 2. After logging in you should be able to upload an excel file (or download the default excel file). The wizard will guide you onwards.
 3. When a dataset is uploaded you can edit it, and query it.

finally

 * **install/package timbuctoo** by copying the target/appassembler folder wherever you like (i.e. `mv timbuctoo-instancev4/target/appassembler/ my-install-location`


### 4. And now...

Check out the [Contribution guidelines](CONTRIBUTING.md) to read more about how to report bugs or ask questions.
Browse the projects source code or read [the API documentation]():

* timbuctoo-instancev4 contains the actual timbuctoo code (it's version 4 already, there used to be a version 3 side by side)
* timbuctoo-test-services contains Hamcrest matchers that aren't really timbuctoo specific. They're a seperate module because of a quirk of history.
* security-client-agnostic is a http-client agnostic version of security-client that we use to do federated authentication
* HttpCommand is a concordion plugin that allows you to write a http request and have concordion execute it. It's what powers the automated validation of our API documentation.
* ContractDiff is another project for our API docs validator. It allows you to specify Json and Http-header contracts. A contract in this context means that you may return more but not less.

## Background

Timbuctoo is funded by

 * The Huygens Institute (indefinite)
 * CLARIAH.nl (until ...)
 * NDE (funding ends december 2016)

