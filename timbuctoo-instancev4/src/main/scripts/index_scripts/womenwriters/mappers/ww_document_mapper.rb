require File.dirname(__FILE__) + '/../../lib/timbuctoo_solr/default_mapper'

class WwDocumentMapper < DefaultMapper
  include ToYearConverter

  def initialize options
    super options
    @cache = {}
  end

=begin
      {label: "author sort", field: "authorNameSort_s"},
      {label: "author", field: "authorName_ss", type: "list-facet", collapse: true},
      {label: "author gender", field: "authorGender_ss", type: "list-facet", collapse: true},
=end

  def convert(record)
    data = super(record)
    unless data['^englishTitle'].nil?
      data['title_t'] = data['title_t'].nil? ? data['^englishTitle'] : "#{data['title_t']} #{data['^englishTitle']}"
      data.delete('^englishTitle')
    end

    p data
    @cache[data['id']] = data
  end



  def find(id)
    @cache[id]
  end
end