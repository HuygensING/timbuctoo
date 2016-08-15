require 'open-uri'
require 'pp'
require './timer.rb'
require './person.rb'
require './persons.rb'
require './document.rb'
require './documents.rb'
require './documentReception.rb'
require './documentReceptions.rb'
require 'json'


if __FILE__ == $0
    Timer.start

    @location = "http://test.repository.huygens.knaw.nl/"
    @person_coll = "wwpersons/"
    @document_coll = "wwdocuments/"
    @pers_reception_coll = "wwpersonreceptions/"
    @doc_reception_coll = "wwdocumentreceptions/"
    @solr_auth_header = ""
    @solr = "http://192.168.99.100:8983/solr/"

    begin
      (0..(ARGV.size-1)).each do |i|
        case ARGV[i]
          when '--debug'
            debug = true
          when '-loc'
            @location = ARGV[i+1]
          when '-solr'
            @solr = "#{ARGV[i+1]}"
          when '-solr-auth'
            @solr_auth_header = "#{ARGV[i+1]}"
          when '-h'
            STDERR.puts "use: ruby scrape_ww_classes.rb -loc location -solr solr-site [--debug]"
            exit(1)
        end
      end
    rescue => detail
      STDERR.puts "#{detail}"
    end

    Person.location = "#{@location}v2.1/"
    Persons.location = "#{@location}v2.1/"
    Persons.solr = "#{@solr}#{@person_coll}"
    Persons.solr_auth = @solr_auth_header
    Persons.debug = true

    Document.location = "#{@location}v2.1/"
    Documents.location = "#{@location}v2.1/"
    Documents.solr_documents = "#{@solr}#{@document_coll}"
    Documents.solr_receptions = "#{@solr}#{@pers_reception_coll}"
    Documents.solr_auth = @solr_auth_header
    Documents.debug = true

    DocumentReceptions.solr = "#{@solr}#{@doc_reception_coll}"
    DocumentReceptions.solr_auth = @solr_auth_header

    continu = true
    start_value = 0
    num_of_lines = 100 # Persons.debug ? 10 : 100

    while(continu)
      continu = Persons.scrape_file start_value,num_of_lines
      start_value += 100 if continu
    end
    Persons.do_solr_commit

    continu = true
    start_value = 0
    num_of_lines = 100 # debug ? 10 : 100

    while(continu)
      continu = Documents.scrape_file start_value,num_of_lines
      start_value += 100 if continu
    end
    Documents.solr_commit "#{@solr}#{@document_coll}"

    DocumentReceptions.create_index
    Timer.stop
end

