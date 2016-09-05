require File.dirname(__FILE__) + '/../../lib/timbuctoo_solr/default_mapper'

class WwPersonMapper < DefaultMapper
  attr_reader :cache
  include ToYearConverter
  include ToNamesConverter

  def initialize options
    super options
    @cache = {}
  end

  def convert(record)
    data = super(record)
    convert_temp_name(data)
    add_location_sort(data)
    @cache[data['id']] = data
  end

  def find(id)
    @cache[id]
  end

  def add_languages(document_mapper)
    @cache.each do |id, record|
      @cache[id]['language_ss'] = []

      record['@workIds'].each do |work_id|
        work = document_mapper.find(work_id)
        if work.nil?
          $stderr.puts "WARNING Problem with work #{work_id} created by author #{id}"
        else
          @cache[id]['language_ss'].concat(work['language_ss'])
        end
      end
      @cache[id]['language_ss'].uniq!
      @cache[id]['languageSort_s'] = @cache[id]['language_ss'].sort.join(" ")
      @cache[id].delete('@workIds')
    end
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
