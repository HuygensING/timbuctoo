require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/default_mapper'

timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl')

@person_mapper = DefaultMapper.new({
   :properties => [
     { :name => '_id', :converted_name => 'id' },
     { :name => '@displayName',  :converted_name => 'displayName_s', :type => 'foo_type'},
   ]
})

def process(record)
  p @person_mapper.convert(record)
end


timbuctoo_io.scrape_collection('wwpersons', :process_record => method(:process))