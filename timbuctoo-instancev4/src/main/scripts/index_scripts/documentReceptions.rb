require "net/http"

class DocumentReceptions

    @@bad_batches = 1
    @@wanted_document_receptions = Array.new
    @@solr = ""
    @@solr_auth = ""

    def DocumentReceptions.add wanted_reception
      @@wanted_document_receptions << wanted_reception
    end

    def DocumentReceptions.get_wanted
      @@wanted_document_receptions
    end

    def DocumentReceptions.create_index
      batch = Array.new
      @@wanted_document_receptions.each do |reception_data|
        reception = DocumentReception.new(reception_data)
        batch << reception
        if batch.length == 100
          DocumentReceptions.do_solr_update batch
          batch = Array.new
        end
      end
      DocumentReceptions.do_solr_update batch

      DocumentReceptions.do_solr_commit
    end


    def DocumentReceptions.delete_index
      puts "DELETE document receptions index"
      uri = URI.parse("#{@@solr}update/")
      req = Net::HTTP::Post.new(uri)
      http = Net::HTTP.new(uri.hostname, uri.port)
      req["Authorization"] = @@solr_auth
      req.content_type = 'text/xml'
      req.body = '<delete><query>*:*</query></delete>'
      response = http.request(req)
      if !response.code.eql?("200")
        puts "SOMETHING WENT WRONG: DocumentReceptions.delete_index"
      end
    end

    def DocumentReceptions.do_solr_update batch
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

    def DocumentReceptions.do_solr_commit
      puts "COMMIT document receptions"
      uri = URI.parse("#{@@solr}update?commit=true")
      req = Net::HTTP::Post.new(uri)
      req["Authorization"] = @@solr_auth
      http = Net::HTTP.new(uri.hostname, uri.port)
      http.request(req)
    end

    def DocumentReceptions.solr= solr
      @@solr = solr
    end

    def DocumentReceptions.solr_auth= solr_auth
      @@solr_auth = solr_auth
    end
end

