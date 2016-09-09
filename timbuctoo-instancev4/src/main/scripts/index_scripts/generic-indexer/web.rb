require 'sinatra'
require 'sinatra/base'

class Web < Sinatra::Base
  get '/' do
    File.read(File.join('static', 'index.html'))
  end

  get '/*.css' do
    response['Content-type'] = 'text/css'
    File.read(File.join('static', "#{params['splat'].first}.css"))
  end

  get '/*.js' do
    response['Content-type'] = 'text/javascript'
    File.read(File.join('static', "#{params['splat'].first}.js"))
  end
end