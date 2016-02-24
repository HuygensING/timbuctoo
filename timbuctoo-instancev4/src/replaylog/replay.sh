#!/usr/bin/env bash

if [ -z "$1" ]; then
  echo "usage $0 ./replayscripts/some-dir"
  exit 1
fi
cd "$1"
MAX="${2:-1000}"

##for now: filter out all requests where:
##  - user_agent = check_http (next version will not be tomcat)
#TO_IGNORE="user-agent: check_http"
##  - accept is text/html (users will accept any new representation)
#TO_IGNORE="$TO_IGNORE\|accept: text/html"
##  - referrer is repository.huygens.knaw.nl (will be replaced. Is usually not a user)
#TO_IGNORE="$TO_IGNORE\|referer: https?://repository.huygens.knaw"
##  - referrer is dutch caribbean (will be replaced)
#TO_IGNORE="$TO_IGNORE\|referer: http://database.dutch-caribbean"
##  - /domain/dcar (wordt gemigreerd)
#TO_IGNORE="$TO_IGNORE\| > [A-Z]\+ /domain/dcar"
##  - vre_id: DutchCaribbean
#TO_IGNORE="$TO_IGNORE\|vre_id: Dutch"
##  - user-agent Yahoo! Slurp
#TO_IGNORE="$TO_IGNORE\|user-agent: Mozilla/5.0 (compatible; Yahoo! Slurp"

TO_ALLOW="POST /v2.1/domain/ww.* HTTP/1.1"

IGNORED=0
COUNT=0
echo -n ""
for file in `ls *.request | head -n "$MAX"`; do
  echo -en "\r\033[K$COUNT successes, and $IGNORED ignored. $file:"

  num=${file%.request}
  if grep -q "$TO_ALLOW" $file; then
    let COUNT=$COUNT+1
  else
    let IGNORED=$IGNORED+1
    continue
  fi

  cat $file | nc 0.0.0.0 8080 > $num.real
  cat $num.real | gsed -e '/^\x0D$/,$d' > $num.realheaders
  realstatus=$(cat $num.realheaders | head -n1 | sed 's|HTTP/1.1 \([0-9]*\).*|\1|')
  expectedstatus=$(cat $num.expectedheaders | head -n1 | grep -o '[0-9]\+')

  if [ "$realstatus" != "$expectedstatus" ]; then
    if [ "$realstatus" = "200" -a "$expectedstatus" = "204" ]; then #it's okay to return an OK instead of NO CONTENT
      true
    else
      if [ $COUNT -gt 1 -o $IGNORED -gt 0 ]; then
        echo ""
      fi
      echo -e "\r\033[K'$realstatus' != '$expectedstatus' => $(cat $num.request | head -n1)"
      COUNT=0
      IGNORED=0
    fi
  fi
done
echo -en "\r\033[K"

#cat replayscripts/catalina.2016-02-17.log/full.log | grep -o '[0-9]* > [A-Z]\+ /[^ ]\+' | grep -o '/[^ ]\+' | gsed 's|[0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f]-[0-9a-f][0-9a-f][0-9a-f][0-9a-f]-[0-9a-f][0-9a-f][0-9a-f][0-9a-f]-[0-9a-f][0-9a-f][0-9a-f][0-9a-f]-[0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f]||g' | sed 's/autocomplete?query=\*.*\*/autocomplete?query=*{}*/g' | sort | uniq
