class WwDocumentConfig

  def WwDocumentConfig.get
    {
      :properties => [
        { :name => '_id',  :converted_name => 'id'},
        { :name => 'title', :converted_name => 'displayName_s'},
        { :name => 'title', :converted_name => 'title_t'},
        { :name => 'documentType', :converted_name => 'documentType_s'},
        { :name => 'englishTitle', :converted_name  => '^englishTitle'},
        { :name => 'date', :converted_name => 'date_i', :type => 'year'},
        { :name => 'notes', :converted_name => 'notes_t'},
        { :name => ['^modified', 'timeStamp'], :converted_name => 'modified_l' },
        { :name => ['^modified', 'userId'], :converted_name => 'modifiedBy_s' },
        { :name => ['^modified', 'username'], :converted_name => 'modifiedBy_s' },
      ], :relations => [
        { :relation_name => 'hasPublishLocation', :property_name => 'displayName', :converted_name => 'publishLocation_ss'},
        { :relation_name => 'hasWorkLanguage', :property_name => 'displayName', :converted_name => 'language_ss'},
        { :relation_name => 'hasGenre', :property_name => 'displayName', :converted_name => 'genre_ss'},
        { :relation_name => 'hasDocumentSource', :property_name => 'displayName', :converted_name => 'source_ss'},

        # Locates creators
        { :relation_name => 'isCreatedBy', :property_name => 'id', :converted_name => '@authorIds'}
      ]
    }
  end
end