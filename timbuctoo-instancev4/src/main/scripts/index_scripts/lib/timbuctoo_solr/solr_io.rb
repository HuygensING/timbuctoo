require 'net/http'
require 'json'

class SolrIO

  # @param [String] base_url the solr base_url (usually including /solr)
  # @param [String] authorization the authorization header
  def initialize(base_url, authorization: nil)
    @base_url = base_url
    @authorization = authorization
  end

  # Creates a new solr index
  # @param [String] index_name name of the index
  # @param [String] config_set the config set used
  def create(index_name, config_set: 'data_driven_schema_configs')
    uri = URI.parse("#{@base_url}/admin/cores?action=CREATE&name=#{index_name}&instanceDir=#{index_name}&configSet=#{config_set}")
    req = Net::HTTP::Post.new(uri)
    req['Authorization'] = @authorization unless @authorization.nil?
    http = Net::HTTP.new(uri.hostname, uri.port)
    response = http.request(req)

    raise "Create of #{index_name} failed for url #{@base_url}" unless response.code.eql?('200')
  end

  # Sends an update request with a payload to solr index (without commit)
  # @param [String] index_name name of the index
  # @param [Hash|Array] payload the Json serializable payload
  def update(index_name, payload)
    uri = URI.parse("#{@base_url}/#{index_name}/update/")
    req = Net::HTTP::Post.new(uri)
    req.content_type = "application/json"
    req['Authorization'] = @authorization unless @authorization.nil?
    http = Net::HTTP.new(uri.hostname, uri.port)
    req.body = payload.to_json
    response = http.request(req)

    raise "Update on #{index_name} failed for url #{@base_url}" unless response.code.eql?('200')
  end

  # Sends a commit request to solr
  # @param [String] index_name name of the index
  def commit(index_name)
    uri = URI.parse("#{@base_url}/#{index_name}/update?commit=true")
    req = Net::HTTP::Post.new(uri)
    req['Authorization'] = @authorization unless @authorization.nil?
    http = Net::HTTP.new(uri.hostname, uri.port)
    response = http.request(req)

    raise "Commit on #{index_name} failed for url #{@base_url}" unless response.code.eql?('200')
  end

  # Deletes all contents of a solr index
  # @param [String] index_name name of the index
  def delete_data(index_name)
    uri = URI.parse("#{@base_url}/#{index_name}/update/")
    req = Net::HTTP::Post.new(uri)
    http = Net::HTTP.new(uri.hostname, uri.port)
    req.content_type = 'text/xml'
    req['Authorization'] = @authorization unless @authorization.nil?
    req.body = '<delete><query>*:*</query></delete>'
    response = http.request(req)

    raise "Delete on #{index_name} failed for url #{@base_url}" unless response.code.eql?('200')
  end

  # Deletes an entire solr index
  # @param [String] index_name name of the index
  def delete_index(index_name)
    uri = URI.parse("#{@base_url}/admin/cores?action=UNLOAD&core=#{index_name}")
    req = Net::HTTP::Post.new(uri)
    req['Authorization'] = @authorization unless @authorization.nil?
    http = Net::HTTP.new(uri.hostname, uri.port)
    response = http.request(req)

    raise "Delete of #{index_name} failed for url #{@base_url}" unless response.code.eql?('200')
  end

end