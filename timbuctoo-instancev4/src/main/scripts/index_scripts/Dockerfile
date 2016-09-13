FROM ubuntu:latest

RUN apt-get update
RUN apt-get install -y build-essential wget git
RUN apt-get install -y zlib1g-dev libssl-dev libreadline-dev libyaml-dev libxml2-dev libxslt-dev
RUN apt-get clean

RUN wget -P /root/src http://cache.ruby-lang.org/pub/ruby/2.2/ruby-2.2.2.tar.gz
RUN cd /root/src; tar xvf ruby-2.2.2.tar.gz
RUN cd /root/src/ruby-2.2.2; ./configure; make install

RUN gem update --system
RUN gem install bundler

RUN mkdir -p /root/app
COPY . /root/app
RUN cd /root/app/generic-indexer; bundle install

EXPOSE 80

ENV TIMBUCTOO_URL http://timbuctoo
ENV SOLR_URL http://solr
ENV PORT 80

CMD ["/usr/local/bin/foreman","start","-d","/root/app/generic-indexer"]

