require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/default_mapper'

timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl')

class FooMapper < DefaultMapper
  def convert_to_foo_type(value)
    "fooified #{value}"
  end
end

@person_mapper = FooMapper.new({
   :properties => [
       { :name => '_id', :converted_name => 'id' },
       { :name => '@displayName',  :converted_name => 'displayName_s', :type => 'foo_type'},
   ],
   :relations => [{
       :relation_name => 'isCreatorOf', # name of the relation to follow
       :property_name => 'displayName', # get the displayName property of the related object
       :converted_name => 'members_ss', # list of strings data type
       :type => 'foo_type'
    }]
})

def process(record)
  p @person_mapper.convert(record)
end


timbuctoo_io.scrape_collection('wwpersons', :process_record => method(:process), :with_relations => true)