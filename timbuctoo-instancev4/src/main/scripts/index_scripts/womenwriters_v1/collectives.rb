require 'net/http'

class Collectives

  @@location = ""
  @@solr = ""
  @@solr_auth = ""
  @@debug = false
  @@collectives = Hash.new

  def Collectives.scrape_file start_value, num_of_lines=100
    filename = "wwcollectives_rows_#{num_of_lines}_start_#{start_value}.json"
    line = nil
    if @@debug and File.exists? filename
      line = File.read filename
    else
      location = "#{@@location}domain/wwcollectives?rows=#{num_of_lines}&start=#{start_value}"
      f = open(location, {:read_timeout=>600})
      line = f.gets
      File.open(filename, 'w') { |file| file.write(line) } if @@debug
    end

    return false if line.eql?("[]")

    array = JSON.parse(line)

    array.each do |obj|
      collective = Collective.new(obj)
      @@collectives[collective.id] = collective
      start_value += 1
    end
    puts "collectives: #{start_value}"
    return !line.eql?("[]")
  end

  def Collectives.create_index
    result = Array.new
    @@collectives.each do |key, collective|
      result << collective
      if result.length == 100
        Collectives.do_solr_post result
        result = Array.new
      end
    end
    Collectives.do_solr_post result if result.length > 0
    Collectives.do_solr_commit
  end

  def Collectives.delete_index
    puts "DELETE collectives index"
    uri = URI.parse("#{@@solr}update/")
    req = Net::HTTP::Post.new(uri)
    http = Net::HTTP.new(uri.hostname, uri.port)
    req.content_type = 'text/xml'
    req["Authorization"] = @@solr_auth
    req.body = '<delete><query>*:*</query></delete>'
    response = http.request(req)
    if !response.code.eql?("200")
      puts "SOMETHING WENT WRONG: Collectives.delete_index"
    end
  end

  def Collectives.do_solr_post batch

    uri = URI.parse("#{@@solr}update/")
    req = Net::HTTP::Post.new(uri)
    req.content_type = "application/json"
    req["Authorization"] = @@solr_auth
    http = Net::HTTP.new(uri.hostname, uri.port)
    req.body = batch.to_json
    response = http.request(req)
    if !response.code.eql?("200")
      puts "SOMETHING WENT WRONG: Collectives.delete_index"
    end
  end

  def Collectives.do_solr_commit
    puts "COMMIT collectives"
    uri = URI.parse("#{@@solr}update?commit=true")
    req = Net::HTTP::Post.new(uri)
    req["Authorization"] = @@solr_auth
    http = Net::HTTP.new(uri.hostname, uri.port)
    http.request(req)
  end

  def Collectives.location= location
    @@location = location
  end

  def Collectives.solr_auth= solr_auth
    @@solr_auth = solr_auth
  end

  def Collectives.solr= solr
    @@solr = solr
  end

  def Collectives.debug= debug
    @@debug = debug
  end

  def Collectives.debug
    @@debug
  end

  def Collectives.size
    @@collectives.size
  end

  def Collectives.all_collectives
    @@collectives
  end

  def Collectives.find id
    @@collectives[id]
  end
end

