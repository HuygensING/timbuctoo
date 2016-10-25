class DcarDocumentConfig
  def DcarDocumentConfig.get
    {
      :properties => [
        { :name => '_id',  :converted_name => 'id'},
        { :name => 'type',  :converted_name => 'type_s'},
        { :name => 'titleNld',  :converted_name => 'displayName_s'},
        { :name => 'titleEng',  :converted_name => 'titleEng_t'},
#        { :name => 'titleEng',  :converted_name => 'titleEng_s_tim'},
        { :name => 'titleNld',  :converted_name => 'titleNld_t'},
#        { :name => 'titleNld',  :converted_name => 'titleNld_s_tim'},
        { :name => 'documentType', :converted_name => 'documentType_s'},
#        { :name => 'date', :converted_name => 'date_i', :type => 'year'},
        { :name => 'notes', :converted_name => 'notes_t'},
#        { :name => 'notes', :converted_name => 'notes_t_tim'},
        { :name => ['^modified', 'timeStamp'], :converted_name => 'modified_l' },
        { :name => ['^modified', 'userId'], :converted_name => 'modifiedBy_s' },
        { :name => ['^modified', 'username'], :converted_name => 'modifiedBy_s' },
      ], :relations => [
       # { :relation_name => 'hasPublishLocation', :property_name => 'displayName', :converted_name => 'publishLocation_ss'},
       # { :relation_name => 'hasWorkLanguage', :property_name => 'displayName', :converted_name => 'language_ss'},
       # { :relation_name => 'hasGenre', :property_name => 'displayName', :converted_name => 'genre_ss'},
       # { :relation_name => 'hasDocumentSource', :property_name => 'displayName', :converted_name => 'source_ss'},

        # Locates creators
        { :relation_name => 'isCreatedBy', :property_name => 'id', :converted_name => '@authorIds'}
      ]
    }
  end
end
