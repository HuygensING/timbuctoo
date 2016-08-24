require 'net/http'

class Persons

    @@location = ""
    @@solr = ""
    @@solr_auth = ""
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

      array = JSON.parse(line)

      array.each do |obj|
          person = Person.new(obj)
          @@persons[person.id] = person
          start_value += 1
      end
      puts "persons: #{start_value}"
      return !line.eql?("[]")
    end

    def Persons.add_languages
      @@persons.each do |key, person|
        person.add_languages
      end
    end

    def Persons.create_index
      result = Array.new
      @@persons.each do |key, person|

        result << person
        if result.length == 100
          Persons.do_solr_post result
          result = Array.new
        end
      end
      Persons.do_solr_post result if result.length > 0
      Persons.do_solr_commit
    end

    def Persons.delete_index
      puts "DELETE persons index"
      uri = URI.parse("#{@@solr}update/")
      req = Net::HTTP::Post.new(uri)
      http = Net::HTTP.new(uri.hostname, uri.port)
      req.content_type = 'text/xml'
      req["Authorization"] = @@solr_auth
      req.body = '<delete><query>*:*</query></delete>'
      response = http.request(req)
      if !response.code.eql?("200")
        puts "SOMETHING WENT WRONG: Persons.delete_index"
      end
    end

    def Persons.do_solr_post batch

      uri = URI.parse("#{@@solr}update/")
      req = Net::HTTP::Post.new(uri)
      req.content_type = "application/json"
      req["Authorization"] = @@solr_auth
      http = Net::HTTP.new(uri.hostname, uri.port)
      req.body = batch.to_json
      http.request(req)
    end

    def Persons.do_solr_commit
      puts "COMMIT persons"
      uri = URI.parse("#{@@solr}update?commit=true")
      req = Net::HTTP::Post.new(uri)
      req["Authorization"] = @@solr_auth
      http = Net::HTTP.new(uri.hostname, uri.port)
      http.request(req)
    end

    def Persons.location= location
      @@location = location
    end

    def Persons.solr_auth= solr_auth
      @@solr_auth = solr_auth
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

