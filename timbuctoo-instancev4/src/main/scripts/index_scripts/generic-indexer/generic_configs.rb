require 'open-uri'
require 'json'

class GenericConfigs

  def initialize(timbuctoo_url:, vre_id:)
    @timbuctoo_url = timbuctoo_url
    @vre_id = vre_id
  end

  def fetch
    open("#{@timbuctoo_url}/v2.1/metadata/#{@vre_id}?withCollectionInfo=true") do |f|
      @metadata = JSON.parse(f.read)
                      .map {|k, md| md}
                      .reject {|md| md['unknown'] or md['relationCollection']}
    end

    open("#{@timbuctoo_url}/v2.1/metadata/Admin?withCollectionInfo=true") do |f|
      @archetype_metadata = JSON.parse(f.read)
                               .map {|k, md| md}
                               .reject {|md| md['unknown'] or md['relationCollection']}
    end

    @metadata.map do |collection_metadata|
      archetype_mapping = @archetype_metadata
                           .select {|amd| amd['archetypeName'].eql?(collection_metadata['archetypeName']) }
                           .first
      {
          :collection => collection_metadata['collectionName'],
          :properties => archetype_mapping['properties']
              .reject {|prop| prop['type'].eql?("relation") }
              .map {|prop| prop['type'].eql?("datable") ?
                    { :name => prop['name'], :converted_name => "#{prop['name']}_i", :type => "year" }
                  : { :name => prop['name'], :converted_name => "#{prop['name']}_s" }
              } <<  { :name => '@displayName', :converted_name => 'displayName_s' }  <<
                    { :name => '_id', :converted_name => 'id' } <<
                    { :name => '@displayName', :converted_name => 'displayName_t' } <<
                    { :name => "^rdfUri", :converted_name => 'rdfUri_s' },
          :relations => archetype_mapping['properties']
              .select {|prop| prop['type'].eql?("relation") }
              .map {|rel| {
                      :relation_name => rel['name'],
                      :property_name => 'displayName',
                      :converted_name => "#{rel['name']}_ss"
              }}
      }
    end
  end
end