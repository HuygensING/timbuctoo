$:.unshift(File.dirname(__FILE__))

require 'web'
require 'rack/reverse_proxy'

use Rack::ReverseProxy do
  reverse_proxy /^\/solr\/?(.*)$/, ENV['SOLR_URL']
end

run Web