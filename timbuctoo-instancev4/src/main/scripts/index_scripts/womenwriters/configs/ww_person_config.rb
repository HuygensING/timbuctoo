class WwPersonConfig
=begin

  {label: "Name", field: "name_t", type: "text"},
      {label: "Person Type", field: "types_ss", type: "list-facet"},
      {label: "Gender", field: "gender_s", type: "list-facet"},
      {label: "Country", field: "relatedLocations_ss", type: "list-facet", collapse: true},
      {label: "Year of birth", field: "birthDate_i", type: "range-facet", collapse: true},
      {label: "Year of death", field: "deathDate_i", type: "range-facet", collapse: true},
      {label: "Place of birth", field: "birthPlace_ss", type: "list-facet", collapse: true},
      {label: "Place of death", field: "deathPlace_ss", type: "list-facet", collapse: true},
      {label: "Children", field: "children_s", type: "list-facet", collapse: true},
      {label: "Provisional notes", field: "notes_t", type: "text", collapse: true}

      {label: "Name", field: "nameSort_s"},
      {label: "Date of birth", field: "birthDate_i"},
      {label: "Modified", field: "modified_l"},
      {label: "Date of death", field: "deathDate_i"},

      {label: "Marital status", field: "maritalStatus_ss", type: "list-facet", collapse: true},
      {label: "Social class", field: "socialClass_ss", type: "list-facet", collapse: true},
      {label: "Education", field: "education_ss", type: "list-facet", collapse: true},
      {label: "Religion/ideology", field: "religion_ss", type: "list-facet", collapse: true},
      {label: "Profession and other activities", field: "profession_ss", type: "list-facet", collapse: true},
      {label: "Financial aspects", field: "financialSituation_ss", type: "list-facet", collapse: true},
      {label: "Memberships", field: "memberships_ss", type: "list-facet", collapse: true},
      {label: "Country", field: "locationSort_s"},

      {label: "Language", field: "language_ss", type: "list-facet", collapse: true},
      {label: "Language", field: "languageSort_s"}

=end
  def WwPersonConfig.get
      {
        :properties => [
            { :name => '_id',  :converted_name => 'id'},
            { :name => 'names', :converted_name => 'name_t', :type => 'names'},
            { :name => 'names', :converted_name => 'nameSort_s', :type => 'name_sort'},
            { :name => 'names', :converted_name => 'displayName_s', :type => 'names_display_name'},
            { :name => 'types', :converted_name => 'types_ss'},
            { :name => 'gender', :converted_name => 'gender_s'},
            { :name => 'birthDate', :converted_name => 'birthDate_i', :type => 'year'},
            { :name => 'deathDate', :converted_name => 'deathDate_i', :type => 'year'},
            { :name => 'notes', :converted_name => 'notes_t'},
            { :name => 'children', :converted_name => 'children_s'},
            { :name => ['^modified', 'timeStamp'], :converted_name => 'modified_l' },
            { :name => '@displayName', :converted_name => '@displayName'}
        ], :relations => [
            { :relation_name => ['hasResidenceLocation', 'hasBirthPlace', 'hasDeathPlace'], :property_name => 'displayName', :converted_name => 'relatedLocations_ss' },
            { :relation_name => 'hasBirthPlace', :property_name => 'displayName', :converted_name => 'birthPlace_ss' },
            { :relation_name => 'hasDeathPlace', :property_name => 'displayName', :converted_name => 'deathPlace_ss' },
            { :relation_name => 'isCreatorOf', :property_name => 'id', :converted_name => '@workIds'}
        ]
      }
    end
end