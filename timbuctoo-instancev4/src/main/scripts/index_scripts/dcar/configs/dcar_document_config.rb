class DcarDocumentConfig
  def DcarDocumentConfig.get
    {
      :properties => [
        { :name => '_id',  :converted_name => 'id'},
        { :name => 'type',  :converted_name => 'type_s'},
        { :name => 'titleEng',  :converted_name => 'titleEng_t'},
        { :name => 'titleNld',  :converted_name => 'titleNld_t'},
        { :name => 'documentType', :converted_name => 'documentType_s'},
        { :name => 'notes', :converted_name => 'notes_t'},
      ], :relations => [
       # { :relation_name => 'hasPublishLocation', :property_name => 'displayName', :converted_name => 'publishLocation_ss'},
       # { :relation_name => 'hasWorkLanguage', :property_name => 'displayName', :converted_name => 'language_ss'},
       # { :relation_name => 'hasGenre', :property_name => 'displayName', :converted_name => 'genre_ss'},
       # { :relation_name => 'hasDocumentSource', :property_name => 'displayName', :converted_name => 'source_ss'},

        # Locates creators
  #      { :relation_name => 'isCreatedBy', :property_name => 'id', :converted_name => '@authorIds'}
      ]
    }
  end
end
