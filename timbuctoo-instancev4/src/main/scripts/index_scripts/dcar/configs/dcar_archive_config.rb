class DcarArchiveConfig
  def DcarArchiveConfig.get
    {
      :properties => [
        { :name => '_id',  :converted_name => 'id'},
        { :name => '@type',  :converted_name => 'type_s'},
        { :name => 'titleEng',  :converted_name => 'titleEng_t'},
        { :name => 'titleEng',  :converted_name => 'titleEng_s'},
        { :name => 'titleNld',  :converted_name => 'titleNld_t'},
        { :name => 'documentType', :converted_name => 'documentType_s'},
        { :name => 'notes', :converted_name => 'notes_t'},
        { :name => 'beginDate', :converted_name => 'beginDate_dt', :type => 'date'},
        { :name => 'endDate', :converted_name => 'endDate_dt', :type => 'date'},
        { :name => 'beginDate', :converted_name => 'beginDate_i', :type => 'year'},
        { :name => 'endDate', :converted_name => 'endDate_i', :type => 'year'},
        { :name => 'refCodeArchive', :converted_name => 'refCodeArchive_s'},
        { :name => 'countries', :converted_name => 'countries_ss' },
        { :name => 'refCode', :converted_name => 'refCode_s'},
        { :name => 'subCode', :converted_name => 'subCode_s'},
        { :name => 'itemNo', :converted_name => 'itemNo_s'},
        { :name => 'series', :converted_name => 'series_s'},
      ], :relations => [
       { :relation_name => 'is_created_by', :property_name => 'displayName', :converted_name => 'creator_ss'},
       { :relation_name => 'has_archive_keyword', :property_name => 'displayName', :converted_name => 'subject_ss'},
       { :relation_name => 'has_archive_person', :property_name => 'displayName', :converted_name => 'person_ss'},
       { :relation_name => 'has_archive_place', :property_name => 'displayName', :converted_name => 'place_ss'},
      ]
    }
  end
end