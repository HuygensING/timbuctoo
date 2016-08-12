require 'net/http'

class Persons

    @@location = ""
    @@solr = ""
    @@debug = false
    @@persons = Hash.new

    def Persons.scrape_file start_value, num_of_lines=100
      filename = "wwpersons_rows_#{num_of_lines}_start_#{start_value}.json"
      line = nil
      if @@debug and File.exists? filename
        line = File.read filename
      else
        location = "#{@@location}domain/wwpersons?rows=#{num_of_lines}&start=#{start_value}&withRelations=true"
        f = open(location, {:read_timeout=>600})
        line = f.gets
        File.open(filename, 'w') { |file| file.write(line) } if @@debug
      end

      return false if line.eql?("[]")

      result = Array.new
      array = JSON.parse(line)

      array.each do |obj|
          person = Person.new(obj)
          @@persons[person.id] = person
          result << person
          start_value += 1
      end
      Persons.do_solr_post result

      return !line.eql?("[]")
    end


    def Persons.do_solr_post batch

      uri = URI.parse("#{@@solr}update/")
      req = Net::HTTP::Post.new(uri)
      req.content_type = "application/json"
      http = Net::HTTP.new(uri.hostname, uri.port)
      req.body = batch.to_json
      http.request(req)
    end

    def Persons.do_solr_commit
      uri = URI.parse("#{@@solr}update?commit=true")
      req = Net::HTTP::Post.new(uri)
      http = Net::HTTP.new(uri.hostname, uri.port)
      http.request(req)
    end

    def Persons.location= location
      @@location = location
    end

    def Persons.solr= solr
      @@solr = solr
    end

    def Persons.debug= debug
      @@debug = debug
    end

    def Persons.debug
      @@debug
    end

    def Persons.size
      @@persons.size
    end

    def Persons.all_persons
      @@persons
    end

    def Persons.find id
      @@persons[id]
    end
end

