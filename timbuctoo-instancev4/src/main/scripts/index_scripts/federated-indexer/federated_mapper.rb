class FederatedMapper < DefaultMapper
  include ToYearConverter
  include ToNamesConverter

  def initialize(collection_name, archetype_name, dataset_name, config)
    super(config)
    @archetype_name = archetype_name
    @collection_name = collection_name
    @dataset_name = dataset_name
  end

  def convert(record)
    converted = super(record)
    converted["archetype_name_s"] = @archetype_name
    converted["dataset_s"] = @dataset_name
    cleaned = {}
    converted.each do |key, value|
      cleaned[key] = value unless value.nil? or (value.is_a?(Array) and value.length == 0)
    end
    cleaned
  end

  def convert_to_collection_bound_id(value)
    "#{@collection_name}/#{value}"
  end

  def convert_to_links(value)
    return "" if value.nil? or value.length == 0
    value.map { |link| "#{link["label"]} #{link["url"]}" }.join(" ")
  end
end