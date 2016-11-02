class DcarLegislationConfig
  def DcarLegislationConfig.get
      {
        :properties => [
            { :name => '_id',  :converted_name => 'id'},
            { :name => '@type',  :converted_name => 'type_s'},
            { :name => 'titleEng', :converted_name => 'titleEng_t'},
            { :name => 'titleNld', :converted_name => 'titleNld_t'},
            { :name => 'contents', :converted_name => 'contents_t'},
            { :name => 'reference', :converted_name => 'reference_t'},
            { :name => 'date1', :converted_name => 'beginDate_s'},
            { :name => 'date2', :converted_name => 'endDate_s'},
            { :name => 'date1', :converted_name => 'beginDate_i', :type => 'year'},
            { :name => 'date2', :converted_name => 'endDate_i', :type => 'year'},
        ], :relations => [
            { :relation_name => 'has_legislation_keyword', :property_name => 'displayName', :converted_name => 'subject_ss' },
            { :relation_name => 'has_legislation_place', :property_name => 'displayName', :converted_name => 'place_ss' },
            { :relation_name => 'has_legislation_person', :property_name => 'displayName', :converted_name => 'person_ss' },
        ]
      }
    end
end
