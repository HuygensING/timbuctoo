require File.dirname(__FILE__) + '/../../lib/timbuctoo_solr/default_mapper'

class WwPersonMapper < DefaultMapper
  include ToYearConverter
  include ToNamesConverter

  def initialize options
    super options
    @cache = {}
  end

=begin
    {label: "Language", field: "language_ss", type: "list-facet", collapse: true},
    {label: "Language", field: "languageSort_s"}
=end


  def convert(record)
    data = super(record)
    convert_temp_name(data)
    add_location_sort(data)
    @cache[data['id']] = data
  end

  def find(id)
    @cache[id]
  end

  private
  def add_location_sort(data)
    data["locationSort_s"] = data["relatedLocations_ss"].sort.join(" ")
  end

  def convert_temp_name(data)
    if data['displayName_s'].empty?
      data['name_t'] = data['@displayName'].sub('[TEMP] ', '')
      data['nameSort_s'] = data['@displayName'].sub('[TEMP] ', '')
      data['displayName_s'] = data['@displayName']
    end
    data.delete('@displayName')
  end

end
