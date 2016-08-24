require "net/http"

class PersonReceptions

  @@bad_batches = 1
  @@wanted_person_receptions = Array.new
  @@solr = ""
  @@solr_auth = ""

  def PersonReceptions.add wanted_reception
    @@wanted_person_receptions << wanted_reception
  end

  def PersonReceptions.get_wanted
    @@wanted_person_receptions
  end

  def PersonReceptions.create_index
    batch = Array.new
    @@wanted_person_receptions.each do |reception_data|
      reception = PersonReception.new(reception_data)
      batch << reception
      if batch.length == 100
        PersonReceptions.do_solr_update batch
        batch = Array.new
      end
    end
    PersonReceptions.do_solr_update batch
    PersonReceptions.do_solr_commit
  end

  def PersonReceptions.delete_index
    puts "DELETE person receptions index"
    uri = URI.parse("#{@@solr}update/")
    req = Net::HTTP::Post.new(uri)
    http = Net::HTTP.new(uri.hostname, uri.port)
    req.content_type = 'text/xml'
    req["Authorization"] = @@solr_auth
    req.body = '<delete><query>*:*</query></delete>'
    response = http.request(req)
    if !response.code.eql?("200")
      puts "SOMETHING WENT WRONG: PersonReceptions.delete_index"
    end
  end

  def PersonReceptions.do_solr_update batch
    uri = URI.parse("#{@@solr}update/")
    req = Net::HTTP::Post.new(uri)
    req.content_type = "application/json"
    req["Authorization"] = @@solr_auth
    http = Net::HTTP.new(uri.hostname, uri.port)
    req.body = batch.to_json
    response = http.request(req)
    if response.code.eql?("400")
      File.open("bad_batch_#{@@bad_batches}.json", 'w') { |file| file.write(batch.to_json) }
      @@bad_batches += 1
      puts "BAD BATCH"
    end
  end

  def PersonReceptions.do_solr_commit
    puts "COMMIT person receptions"
    uri = URI.parse("#{@@solr}update?commit=true")
    req = Net::HTTP::Post.new(uri)
    req["Authorization"] = @@solr_auth
    http = Net::HTTP.new(uri.hostname, uri.port)
    http.request(req)
  end

  def PersonReceptions.solr= solr
    @@solr = solr
  end

  def PersonReceptions.solr_auth= solr_auth
    @@solr_auth = solr_auth
  end
end

