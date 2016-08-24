require 'net/http'

class Documents

    @@location = ""
    @@solr_documents = ""
    @@solr_auth = ""
    @@debug = false
    @@number = 0
    @@documents = Hash.new

    def Documents.scrape_file start_value, num_of_lines=100
      filename = "wwdocuments_rows_#{num_of_lines}_start_#{start_value}.json"
      location = "#{@@location}domain/wwdocuments?rows=#{num_of_lines}&start=#{start_value}&withRelations=true"

      line = nil
      if @@debug and File.exists? filename
        line = File.read filename
      else
        f = open(location, {:read_timeout=>600})
        line = f.gets
        File.open(filename, 'w') { |file| file.write(line) } if @@debug
      end
      return false  if line.eql?("[]")

      array = JSON.parse(line)
      array.each do |obj|
          res = Document.new(obj)
          @@documents[res['id']] = res
          @@number += 1
          start_value += 1
      end
      puts "documents: #{start_value}"
      return !line.eql?("[]")
    end

    def Documents.add_creators
      @@documents.each do |key, document|
        document.add_creators
      end
    end

    def Documents.create_index
      result = Array.new
      @@documents.each do |key, document|

        result << document
        if result.length == 100
          Documents.do_solr_post result
          result = Array.new
        end
      end
      Documents.do_solr_post result if result.length > 0
      Documents.do_solr_commit
    end

    def Documents.delete_index
      puts "DELETE documents index"
      uri = URI.parse("#{@@solr_documents}update/")
      req = Net::HTTP::Post.new(uri)
      http = Net::HTTP.new(uri.hostname, uri.port)
      req.content_type = 'text/xml'
      req["Authorization"] = @@solr_auth
      req.body = '<delete><query>*:*</query></delete>'
      response = http.request(req)
      if !response.code.eql?("200")
        puts "SOMETHING WENT WRONG: Documents.delete_index"
      end
    end

    def Documents.do_solr_post batch
      uri = URI.parse("#{@@solr_documents}update/")
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

    def Documents.do_solr_commit
      uri = URI.parse("#{@@solr_documents}update?commit=true")
      req = Net::HTTP::Post.new(uri)
      req["Authorization"] = @@solr_auth
      http = Net::HTTP.new(uri.hostname, uri.port)
      http.request(req)
      puts "COMMIT documents"
    end

    def Documents.location= location
      @@location = location
    end

    def Documents.solr_documents= solr
      @@solr_documents = solr
    end

    def Documents.solr_auth= solr_auth
      @@solr_auth = solr_auth
    end

    def Documents.debug= debug
      @@debug = debug
    end

    def Documents.number
      @@number
    end

    def Documents.find id
      @@documents[id]
    end
end

