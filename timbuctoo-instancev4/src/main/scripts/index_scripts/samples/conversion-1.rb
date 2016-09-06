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