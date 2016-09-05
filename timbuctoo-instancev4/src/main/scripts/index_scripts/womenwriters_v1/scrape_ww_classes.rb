require 'open-uri'
require 'pp'
require './timer.rb'
require './person.rb'
require './persons.rb'
require './document.rb'
require './documents.rb'
require './documentReception.rb'
require './documentReceptions.rb'
require './personReception.rb'
require './personReceptions.rb'
require './collective.rb'
require './collectives.rb'
require 'json'


if __FILE__ == $0

    @location = "http://test.repository.huygens.knaw.nl/"
    @person_coll = "wwpersons/"
    @document_coll = "wwdocuments/"
    @collective_coll = "wwcollectives/"
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
    Persons.debug = false

    Document.location = "#{@location}v2.1/"
    Documents.location = "#{@location}v2.1/"
    Documents.solr_documents = "#{@solr}#{@document_coll}"
    Documents.solr_auth = @solr_auth_header
    Documents.debug = false
    
    Collective.location = "#{@location}v2.1/"
    Collectives.location = "#{@location}v2.1/"
    Collectives.solr = "#{@solr}#{@collective_coll}"
    Collectives.solr_auth = @solr_auth_header
    Collectives.debug = false

    DocumentReceptions.solr = "#{@solr}#{@doc_reception_coll}"
    DocumentReceptions.solr_auth = @solr_auth_header

    PersonReceptions.solr = "#{@solr}#{@pers_reception_coll}"
    PersonReceptions.solr_auth = @solr_auth_header

    continu = true
    start_value = 0
    num_of_lines = 100 # Persons.debug ? 10 : 100

    while(continu)
      continu = Collectives.scrape_file start_value,num_of_lines
      start_value += 100 if continu
    end
    Collectives.delete_index
    Collectives.create_index

    continu = true
    start_value = 0
    num_of_lines = 100 # debug ? 10 : 100

    while(continu)
      continu = Persons.scrape_file start_value,num_of_lines
      start_value += 100 if continu
    end

    continu = true
    start_value = 0
    num_of_lines = 100 # debug ? 10 : 100

    while(continu)
      continu = Documents.scrape_file start_value,num_of_lines
      start_value += 100 if continu
    end

    # Always run Persons.add_languages before Documents.add_creators to ensure correct _childDocuments_ filters
    # on wwdocuments index and wwdocumentreceptions index!!
    Persons.add_languages
    Documents.add_creators

    Persons.delete_index
    Persons.create_index

    Documents.delete_index
    Documents.create_index

    PersonReceptions.delete_index
    PersonReceptions.create_index

    DocumentReceptions.delete_index
    DocumentReceptions.create_index
end

