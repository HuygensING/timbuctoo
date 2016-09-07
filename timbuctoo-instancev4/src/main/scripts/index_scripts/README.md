# Solr index script

This folder contains scripts that scrape timbuctoo and send the data to solr.

## Context
We want to be able expose Timbuctoo collections through solr/lucene in a flexible way,
enabling indexes on both the direct properties of records in a collection, as well as
key properties of directly related objects; for instance a keyword value from a keyword 
collection, which was linked to a document from a document collection (like genre).


## Usage
Creating an index for a Timbuctoo collection is a 3-step process:

1. [Scrape](#scraping) Timbuctoo data
2. [Convert](#converting) to solr docs
3. Send [solr](#solr) docs to solr server

For the impatient: [skip to complete example](#indexing).

### Libraries
For that purpose there are three classes in the dir ```lib/timbuctoo_solr```.

1. TimbuctooIO ```lib/timbuctoo_solr/timbuctoo_io.rb```
2. DefaultMapper ```lib/timbuctoo_solr/default_mapper.rb```
3. SolrIO ```lib/timbuctoo_solr/solr_io.rb```

The next sections will explain the 3-step process in more detail, using examples which 
can be found in the ```samples``` dir.

### Scraping
A scrape of a Timbuctoo collection is done in batches using the method 
```scrape_collection``` in TimbuctooIO. This can either be done with or without
direct relations:

1. [Sample scrape response without relations](http://test.repository.huygens.knaw.nl/v2.1/domain/dcararchives?rows=10)
2. [Sample with relations](http://test.repository.huygens.knaw.nl/v2.1/domain/dcararchives?rows=10&withRelations=true)

#### Basic scrape
This script above scrapes the collection 'dcararchives' from the Timbuctoo test repository:
```ruby
# samples/basic-scrape.rb
require '../lib/timbuctoo_solr/timbuctoo_io'

timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl')
timbuctoo_io.scrape_collection('dcararchives')    
```
Its default behaviour is to fetch the records in batches of 100, without relations, dumping
each record individually to standard output.

Sample of the output:
```
{"beginDate"=>"1700", "countries"=>["NL"], "endDate"=>"1800", "extent"=>"1 folder", "itemNo"=>"9169", "madeBy"=>"MS", "notes"=>"18th century.", "origFilename"=>"/data/data_Atlantische_wereld/Archieven/Archief_Nederlandse_Jezuieten_Nijmegen/Handschriftenverzameling/AD9_9169", "refCode"=>"AD.9", "refCodeArchive"=>"Archief Nederlandse Jezuieten", "reminders"=>"Gegevens ontvangen van Hans de Valk, 19-4-2007", "titleEng"=>"Documents relating to the RC mission on Curaçao in the 18th century", "titleNld"=>"Stukken betreffende de missie op Curaçao in de 18e eeuw", "@displayName"=>"Stukken betreffende de missie op Curaçao in de 18e eeuw", "^rev"=>1, "^modified"=>{"timeStamp"=>1411642687699, "userId"=>"importer", "vreId"=>"dcar"}, "^created"=>{"timeStamp"=>1411642687699, "userId"=>"importer", "vreId"=>"dcar"}, "@variationRefs"=>[{"id"=>"778bb9f8-a4fa-4a55-aed3-997da73112a0", "type"=>"archive"}, {"id"=>"778bb9f8-a4fa-4a55-aed3-997da73112a0", "type"=>"dcararchive"}], "^deleted"=>false, "_id"=>"778bb9f8-a4fa-4a55-aed3-997da73112a0"}
{"beginDate"=>"1670", "countries"=>["NL"], "endDate"=>"1870", "extent"=>"1 folder", "itemNo"=>"9170", "madeBy"=>"MS", "notes"=>"Undated.", "origFilename"=>"/data/data_Atlantische_wereld/Archieven/Archief_Nederlandse_Jezuieten_Nijmegen/Handschriftenverzameling/AD10_9170", "refCode"=>"AD.10", "refCodeArchive"=>"Archief Nederlandse Jezuieten", "reminders"=>"Gegevens ontvangen van Hans de Valk, 19-4-2007", "titleEng"=>"(Handwritten) notes concerning the Jesuit mission and missionaries in Suriname and Curaçao during the Republic and in the 19th century", "titleNld"=>"(Handgeschreven) aantekeningen betreffende de missie en missionarissen SJ in Suriname en Curaçao zowel onder de Republiek als in de 19e eeuw", "@displayName"=>"(Handgeschreven) aantekeningen betreffende de missie en missionarissen SJ in Suriname en Curaçao zowel onder de Republiek als in de 19e eeuw", "^rev"=>1, "^modified"=>{"timeStamp"=>1411642687699, "userId"=>"importer", "vreId"=>"dcar"}, "^created"=>{"timeStamp"=>1411642687699, "userId"=>"importer", "vreId"=>"dcar"}, "@variationRefs"=>[{"id"=>"bead3064-ada9-4ee5-aad0-e5a926026574", "type"=>"archive"}, {"id"=>"bead3064-ada9-4ee5-aad0-e5a926026574", "type"=>"dcararchive"}], "^deleted"=>false, "_id"=>"bead3064-ada9-4ee5-aad0-e5a926026574"}
```



#### Configuring the scrape
The code block below documents some options exposed by TimbuctooIO to alter scraping behaviour.
```ruby
# samples/basic-scrape.rb

# Will dump scraped files (json) to specified :dump_dir
timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl', {
    :dump_files => true,
    :dump_dir => './'
})
timbuctoo_io.scrape_collection('dcararchives', {
    :with_relations => true, # also scrape direct relations
    :batch_size => 1000, # scrape in batches of 1000
})
```

Dumping files has the advantage of not having to re-scrape the collection during development. 
The filenames of the dump files have a signature reflecting the parameters of the scrape.
For instance, the above example outputs files with this format: ```dcararchives_rows_1000_start_1000_with_relations.json```

#### Re-scraping from locally dumped files
To scrape from the locally dumped files in stead of Timbuctoo, add the ```:from_file``` flag to the
```scrape_collection``` method. In this case the value of ```:dump_dir``` in the constructor must
match the location of the dumped files. If the (some of the) files are not present, TimbuctooIO
will fall back on scraping the Timbuctoo server. 
```ruby
# samples/basic-scrape.rb

timbuctoo_io.scrape_collection('dcararchives', {
    :with_relations => true, # also scrape direct relations
    :batch_size => 1000, # scrape in batches of 1000
    :from_file => true # scrape from local file dump in stead of Timbuctoo, if files are present
})
```

#### The 'process_record' callback
Each record scraped by ```scrape_collection``` is passed to a callback function, identified by 
the ```:process_record``` keyword argument. The default behaviour of ```process_record``` is
to dump the (json deserialized) record to standard output (as seen above). 
The way to access the records in order to convert them is to set the ```:process_record```
parameter to a lambda function. The next examples illustrate this:

```ruby
# samples/scrape-callbacks.rb
require 'json'
require '../lib/timbuctoo_solr/timbuctoo_io'

timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl')


def process_record_callback(record)
  puts record.to_json
end

timbuctoo_io.scrape_collection('dcararchives', {
    :process_record => lambda {|record| process_record_callback(record) } # lambda syntax
})

timbuctoo_io.scrape_collection('dcararchives', {
    :process_record => -> (record) { process_record_callback(record) } # lambda shorthand syntax
})

timbuctoo_io.scrape_collection('dcararchives', {
    :process_record => method(:process_record_callback) # using lambda generator utility method 'method'
})


class RecordProcessor
  def process_record_callback(record)
    puts record.to_json
  end
end

record_processor = RecordProcessor.new
timbuctoo_io.scrape_collection('dcararchives', {
    :process_record => record_processor.method(:process_record_callback) # referencing a method in a different class instance
})
```


### Converting
To convert records from Timbuctoo format to a format that can be indexed into solr the DefaultMapper can be used.
The DefaultMapper expects a configuration upon construction, telling it which properties to map to a solr field.

#### Mapping direct properties using DefaultMapper
This example shows how to map Timbuctoo record properties to a solr format using the [data_driven_schema_configs](https://cwiki.apache.org/confluence/display/solr/Schemaless+Mode)
config set of Solr 6. This format uses field name suffixes to identify data types, and the field named 'id' as 
for  uniqueness constraint.

This first example illustrates conversion of some properties in the 'wwcollectives' collection
```ruby
# samples/conversion-1.rb
require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/default_mapper'

timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl')

@collectives_mapper = DefaultMapper.new({
  :properties => [ # configure direct properties of Timbuctoo record
      {
          :name => '_id', # the property name in the Timbuctoo data
          :converted_name => 'id' # the name used for Solr
      },
      { :name => 'name',  :converted_name => 'name_t'}, # name field for full-text search
      { :name => 'type',  :converted_name => 'type_s'}, # type field as string for filtering
      { :name => '@displayName',  :converted_name => 'displayName_s'}, # the human readable display name
      {
          :name => [ '^modified', 'timeStamp' ], # a nested property ({ "^modified": {"timeStamp": ... }})
          :converted_name => 'modified_l' # this field is of type long
      }
  ]
})

def process(record)
  p @collectives_mapper.convert(record)
end


timbuctoo_io.scrape_collection('wwcollectives', :process_record => method(:process))
```

Some samples of the output:
```
{"id"=>"bead82cb-3396-4194-8e01-c965d21314d5", "name_t"=>"Suomen Naisyhdistys", "type_s"=>"ASSOCIATION", "displayName_s"=>"Suomen Naisyhdistys", "modified_l"=>1457519077226}
{"id"=>"3750675a-8a6f-4bca-84b3-c8a0f8927a24", "name_t"=>"Naisasialiitto Unioni", "type_s"=>"ASSOCIATION", "displayName_s"=>"Naisasialiitto Unioni", "modified_l"=>1457519140679}
{"id"=>"5040485d-58d2-4539-968c-8bf7182f83ba", "name_t"=>"Suomalainen naisliitto", "type_s"=>"ASSOCIATION", "displayName_s"=>"Suomalainen naisliitto", "modified_l"=>1457521488510}
```

#### Converting the value of a Timbuctoo property
The DefaultMapper configuration can be supplied with a type parameter per property. It ships one supported data type (int)
which will pass the property value to the ```convert_to_int``` method, which attempts to cast the value using ```to_i```.

This example converts the string value of 'birthDate' into the int value of 'birthDate_i'
```ruby
# samples/conversion-2.rb

@person_mapper = DefaultMapper.new({
  :properties => [
    { :name => '_id', :converted_name => 'id' },
    { :name => '@displayName',  :converted_name => 'displayName_s'},
    { :name => 'birthDate', :converted_name => 'birthDate_i', :type => 'int' }
  ]
})
```

Asking the DefaultMapper to convert to any other type than 'int' will raise the following message:
```
default_mapper.rb:55:in `convert_value': Type 'your_type' not supported please define method convert_to_your_type (RuntimeError)
```

As illustrated in ```samples/conversion-raise.rb```.

To remedy this we recommend inheriting from DefaultMapper and implementing the method 'convert_to_your_type':
```ruby
# samples/type-conversion-1.rb

class FooMapper < DefaultMapper
  def convert_to_foo_type(value)
    "fooified #{value}"
  end
end

@person_mapper = FooMapper.new({
   :properties => [
       { :name => '_id', :converted_name => 'id' },
       { :name => '@displayName',  :converted_name => 'displayName_s', :type => 'foo_type'},
   ]
})
```

For some common data types modules are provided in ```lib/mixins/converters```, delegating the responsibility of hand-writing
type converters. Ruby modules can be used as mixins inside a class using the ```include``` instruction.
```ruby
# samples/type-conversion-2.rb

require '../lib/mixins/converters/to_names_converter'
require '../lib/mixins/converters/to_year_converter'

class PersonMapper < DefaultMapper
  include ToNamesConverter
  include ToYearConverter
end

@person_mapper = PersonMapper.new({
  :properties => [
    { :name => '_id', :converted_name => 'id' },
    { :name => 'birthDate', :converted_name => 'birthDate_i', :type => 'year' },
    { :name => 'names', :converted_name => 'name_t', :type => 'names'},
    { :name => 'names', :converted_name => 'nameSort_s', :type => 'name_sort'},
    { :name => 'names', :converted_name => 'displayName_s', :type => 'names_display_name'},
  ]
})
```

When writing a custom converter which has a good chance of being reused by another indexer, we recommend
adding this converter in a similar module under ```lib/mixins/converters```.


#### Mapping properties of direct relations
The DefaultMapper can also be configured with properties derived from directly related objects. This requires that
```TimbuctooIO.scrape_collection``` is invoked with ```{ :with_relations => true }```.

Example:
```ruby
# samples/conversion-with-relations.rb

@collectives_mapper = DefaultMapper.new({
  :properties => [
    { :name => '_id', :converted_name => 'id' },
    { :name => '@displayName',  :converted_name => 'displayName_s'}
  ],
  :relations => [
    {
      :relation_name => 'hasMember', # name of the relation to follow
      :property_name => 'displayName', # get the displayName property of the related object
      :converted_name => 'members_ss' # list of strings data type
    },
    {
      :relation_name => 'hasMember', # name of the relation to follow
      :property_name => 'path', # get the path property to the related object
      :converted_name => 'memberId_ss' # list of strings data type
    }
  ]
})
```

Sample of the output
```
{"id"=>"3286ea24-d4fb-4c94-8a06-e04b8aa5741b", "displayName_s"=>"Accademia degli Arcadia", "members_ss"=>["Paolina Secco Suardo Grismondi", "Hélène Baletti Riccoboni"], "memberId_ss"=>["domain/wwpersons/676c4572-25ce-4c19-af97-903a08e388e8", "domain/wwpersons/8b1e5848-cf76-448c-9427-5221dba236ef"]}
{"id"=>"769cd459-63bb-4b32-bc82-3630dfe3ec64", "displayName_s"=>"Academies of Rouen, Lyon, Bologne, Padoue, Cortone, Florence, Rome Arcadia (Briquet)", "members_ss"=>["Anne-Marie du Boccage"], "memberId_ss"=>["domain/wwpersons/16b64aed-2d82-481b-9796-56cb51b71711"]}
{"id"=>"f3fb32d9-b28b-4aa8-88f9-d8945e44a1bb", "displayName_s"=>"Other : Political party", "members_ss"=>[], "memberId_ss"=>[]}
```

The same type conversion rules apply to properties derived from relations (see: ```samples/type-conversion-1.rb```).

### Solr
The class SolrIO exposes a few basic CRUD methods. The samples in this section assumes a local running solr 6 server ([quickstart](http://lucene.apache.org/solr/quickstart.html)).
The constructor is invoked with the solr url (usually including /solr without trailing slash). Optionally a value for the header 'Authorization' 
can be added in the constructor as well.

#### CRUD methods of SolrIO
Use the ```create``` method to create an index. By default an index is created with the config set 'data_driven_schema_configs'
If the index already exists, this method will raise an exception.

Use the ```update``` method to send a batch of data to be indexed.

Use the ```delete_data``` method to delete all contents of an index

Use the ```commit``` method to commit the changes that were sent.

Use the ```delete_index``` method to purge the entire index from solr.

Example:
```ruby
# samples/solr.rb
require '../lib/timbuctoo_solr/solr_io'

# Initialize for local solr
solr_io = SolrIO.new('http://localhost:8983/solr')

# Create index named 'testing'
solr_io.create('testing')

# Update index with batch of one record
solr_io.update('testing', [{:id => "foobar", :value_i => 123}])
solr_io.commit('testing')

# Throw away the data
solr_io.delete_data('testing')

solr_io.commit('testing')

solr_io.delete_index('testing')
```


### Indexing
This sample code integrates most of the pieces listed above into one sample indexer. If you skipped directly to this section,
please be aware that this sample does not illustrate all the possibilities.

```ruby
# samples/indexer.rb
require 'open-uri'

require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/default_mapper'
require '../lib/timbuctoo_solr/solr_io'


class Indexer

  def initialize
    @timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl')
    @solr_io = SolrIO.new('http://localhost:8983/solr')

    @mapper = DefaultMapper.new({
        :properties => [
            { :name => '_id', :converted_name => 'id' },
            { :name => '@displayName',  :converted_name => 'displayName_s'},
            { :name => [ '^modified', 'timeStamp' ], :converted_name => 'modified_l'}
        ],
        :relations => [
            {
                :relation_name => 'has_archive_keyword', # name of the relation to follow
                :property_name => 'displayName', # get the path property to the related object
                :converted_name => 'keyword_ss' # list of strings data type
            }
        ]
    })
  end

  def run
    @solr_io.create('testing')
    @timbuctoo_io.scrape_collection('dcararchives', :with_relations => true, :process_record => method(:process))
    @solr_io.commit('testing')
    @solr_io.delete_index('testing')
  end

  def process(record)
    @solr_io.update('testing', [@mapper.convert(record)])
  end
end

Indexer.new.run
```

A more elaborate example is in ```samples/complete-sample-runner.rb```. 



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
