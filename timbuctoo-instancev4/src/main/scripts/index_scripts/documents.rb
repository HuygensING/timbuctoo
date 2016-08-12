require 'net/http'

class Documents

    @@location = ""
    @@solr_documents = ""
    @@solr_receptions = ""
    @@solr_doc_receptions = ""
    @@debug = false
    @@number = 0
    @@count_doc_rels = 0
    @@documents = Hash.new
    @@document_receptions = Array.new
    @@complete_document_receptions = Array.new
    @@person_receptions = Array.new

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

      result = Array.new
      array = JSON.parse(line)
      array.each do |obj|
          res = Document.new(obj)
          @@documents[res['id']] = res
          result << res
          @@number += 1
          start_value += 1
      end
      Documents.do_solr_update result, @@solr_documents
      return !line.eql?("[]")
    end


    def Documents.do_solr_update batch,location
      uri = URI.parse("#{location}update/")
      req = Net::HTTP::Post.new(uri)
      req.content_type = "application/json"
      http = Net::HTTP.new(uri.hostname, uri.port)
      req.body = batch.to_json
      http.request(req)
    end

    def Documents.solr_commit location, debug=false
      uri = URI.parse("#{location}update?commit=true")
      req = Net::HTTP::Post.new(uri)
      http = Net::HTTP.new(uri.hostname, uri.port)
      http.request(req)
    end

    def Documents.location= location
    @@location = location
    end

    def Documents.solr_documents= solr
    @@solr_documents = solr
    end

    def Documents.solr_receptions= solr
    @@solr_receptions = solr
    end

    def Documents.solr_receptions
    @@solr_receptions
    end

    def Documents.solr_doc_receptions= solr
    @@solr_doc_receptions = solr
    end

    def Documents.solr_doc_receptions
    @@solr_doc_receptions
    end

    def Documents.person_receptions_concat data
    @@person_receptions += data
    end

    def Documents.document_receptions_concat data
    @@document_receptions += data
    end

    def Documents.document_receptions
    @@document_receptions
    end

    def Documents.complete_document_receptions_add data
    @@complete_document_receptions << data
    end

    def Documents.complete_document_receptions
    @@complete_document_receptions
    end

    def Documents.complete_document_receptions_reset
    @@complete_document_receptions = Array.new
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

    def Documents.count_doc_rels_inc
    @@count_doc_rels += 1
    end

    def Documents.count_doc_rels
    @@count_doc_rels
    end
end

