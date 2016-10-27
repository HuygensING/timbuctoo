require File.dirname(__FILE__) + '/../../lib/timbuctoo_solr/default_mapper'
require File.dirname(__FILE__) + '/../../lib/mixins/converters/to_year_converter'
require File.dirname(__FILE__) + '/../../lib/mixins/converters/to_names_converter'

class DcarArchiveMapper < DefaultMapper
  include ToYearConverter
  include ToNamesConverter

  attr_reader :cache, :record_count

  def initialize options
    super options
    @cache = {}
    @record_count = 0
  end

  def convert(record)
    data = super(record)
    data['type_s'] = 'archive'
#    convert_temp_name(data)
#    add_location_sort(data)

    puts "Archive scrape: #{@record_count}" if @record_count % 100 == 0
    @record_count += 1
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
          $stderr.puts "WARNING Problem with work #{work_id} created by author #{id} (wrong VRE?)"
        else
          @cache[id]['language_ss'].concat(work['language_ss'])
        end
      end
      @cache[id]['language_ss'].uniq!
      @cache[id]['languageSort_s'] = @cache[id]['language_ss'].sort.join(" ")
      @cache[id].delete('@workIds')
    end
  end

  def send_cached_batches_to(index_name, batch_callback)
    batch_size = 500
    batch = []
    @cache.each do |key, record|
      batch << record
      if batch.length >= batch_size
        batch_callback.call(index_name, batch)
        batch = []
      end
    end
    batch_callback.call(index_name, batch)
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
