require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/default_mapper'

timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl')

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

def process(record)
  p @collectives_mapper.convert(record)
end


timbuctoo_io.scrape_collection('wwcollectives', :process_record => method(:process), :with_relations => true)