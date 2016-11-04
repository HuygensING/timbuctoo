require 'net/http'
require 'json'

class Dataset

  attr_reader :metadata, :name

  def initialize(name: nil, metadata: nil, base_url: nil)
    @name = name
    @base_url = base_url
    @metadata = fetch_metadata(metadata)
  end

  private
  def fetch_metadata(path)
    location = "#{@base_url}#{path}?withCollectionInfo=true"
    uri = URI.parse(location)
    req = Net::HTTP::Get.new(uri)
    http = Net::HTTP.new(uri.hostname, uri.port)

    response = http.request(req)
    raise "http request to #{location} failed with status #{response.code}: #{location}" unless response.code.eql?('200')
    JSON.parse(response.body, :symbolize_names => true)
  end
end

class TimbuctooIO

  # @param [String] base_url the timbuctoo server base url
  # @params [Boolean] dump_files flag for dumping files
  # @params [String] dump_dir the directory to dump the files in
  def initialize (base_url, dump_files: false, dump_dir: './')
    @base_url = base_url
    @dump_files = dump_files
    @dump_dir = dump_dir || './'
  end


  # Scrapes an entire Timbuctoo collection
  # @param [String] collection_name the name of the timbuctoo collection
  # @param [lambda] process_record the function to invoke for each single record in the batch
  # @param [Boolean] with_relations also scrape the direct relations of the records
  # @param [Integer] batch_size number of records per batch
  # @param [Boolean] from_file read from local file in stead of from http if the file is present
  def scrape_collection (collection_name,
                         process_record: lambda {|record| puts record.inspect },
                         with_relations: false,
                         batch_size: 100,
                         from_file: false)

    start_value = 0
    data = nil
    while data.nil? or data.length > 0
      json_data = nil
      json_data = get_file_batch(batch_size, collection_name, start_value, with_relations) if from_file
      json_data = get_http_batch(batch_size, collection_name, start_value, with_relations) if json_data.nil?

      dump_to_file(collection_name, batch_size, start_value, with_relations, json_data) if @dump_files

      data = JSON.parse(json_data)

      data.each {|record| process_record.call(record) }

      start_value = start_value + batch_size
    end
  end

  def fetch_datasets
    location = "#{@base_url}/v2.1/system/vres"
    uri = URI.parse(location)
    req = Net::HTTP::Get.new(uri)
    http = Net::HTTP.new(uri.hostname, uri.port)

    response = http.request(req)
    raise "http request to #{location} failed with status #{response.code}: #{location}" unless response.code.eql?('200')

    JSON.parse(response.body, :symbolize_names => true)
        .map{|dataset_data| Dataset.new(dataset_data.merge({:base_url => @base_url}))}
  end


  private
  def get_file_batch(batch_size, collection_name, start_value, with_relations)
    filename = get_dump_filename(batch_size, collection_name, start_value, with_relations)
    return File.read filename if File.exists? filename
    nil
  end

  def get_http_batch(batch_size, collection_name, start_value, with_relations)
    location = "#{@base_url}/v2.1/domain/#{collection_name}" +
        "?rows=#{batch_size}" +
        "&start=#{start_value}" +
        (with_relations ? '&withRelations=true' : '')
    uri = URI.parse(location)
    req = Net::HTTP::Get.new(uri)
    http = Net::HTTP.new(uri.hostname, uri.port)

    response = http.request(req)
    raise "http request to  #{location} failed with status #{response.code}: #{location}" unless response.code.eql?('200')
    response.body
  end

  def dump_to_file (collection_name, batch_size, start_value, with_relations, response_body)
    filename = get_dump_filename(batch_size, collection_name, start_value, with_relations)
    File.open(filename, 'w') { |file| file.write(response_body) }
  end

  def get_dump_filename(batch_size, collection_name, start_value, with_relations)
    "#{@dump_dir.sub(/\/$/, '')}/" +
        "#{collection_name}_rows_#{batch_size}_start_#{start_value}" +
        "#{with_relations ? '_with_relations' : ''}.json"
  end

end