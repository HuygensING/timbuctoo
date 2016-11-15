require 'open-uri'
require 'json'

class GenericConfigs

  def GenericConfigs.conversion_configs
    {
      :text => [{ :postfix => "_t" }, {:postfix => "_s"}],
      :select => [{ :postfix => "_s" }],
      :multiselect => [{ :postfix => "_ss", }],
      :names => [{ :postfix => "_t", :converter_type => "names" }],
      :datable => [
        { :postfix => "_i", :converter_type => "year" },
        { :postfix => "_s" }
      ],
      :links => [{ :postfix => "_t",  :converter_type => "links" }],
      'list-of-strings'.to_sym => [{:postfix => "_ss" }]
    }
  end

  def initialize(timbuctoo_url:, vre_id:)
    @timbuctoo_url = timbuctoo_url
    @vre_id = vre_id
  end

  def make_property_configs(properties)
    property_configs = [
      {:name => "_id", :converted_name => "id"},
      {:name => "^rdfUri", :converted_name => 'rdfUri_s'},
      {:name => '@displayName', :converted_name => 'displayName_s'},
      {:name => '@displayName', :converted_name => 'displayName_t'},
    ]

    # looks up the correct solr postfix and converter in conversions_configs
    properties.reject { |prop| prop["type"].eql?("relation") }.each do |prop|
      GenericConfigs.conversion_configs[prop["type"].to_sym].each do |conf|
        property_configs << {
          :name => prop["name"],
          :converted_name => "#{prop["name"]}#{conf[:postfix]}",
          :type => conf.key?(:converter_type) ? conf[:converter_type] : nil
        }
      end
    end
    property_configs
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
          :properties => make_property_configs(archetype_mapping['properties'].reject {|prop| prop['type'].eql?("relation")}),
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