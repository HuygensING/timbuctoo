class SolrUpdater
  def initialize(index_name, solr_io, mapper)
    @index_name = index_name
    @solr_io = solr_io
    @mapper = mapper
    @batch = []
  end

  def add (record)
    @batch << @mapper.convert(record)

    if @batch.size >= 1000
      @solr_io.update(@index_name, @batch)
      @batch = []
    end
  end


  def flush
    @solr_io.update(@index_name, @batch)
  end
end