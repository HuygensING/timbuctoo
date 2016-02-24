#!/usr/bin/env bash

if [ -z "$1" ]; then
  echo "usage $0 ./catalina.some-da-te.log"
  exit 1
fi
SERVERPREFIX=http://hi12.huygens.knaw.nl:8080/timbuctoo


if which gsed > /dev/null; then
  sed=gsed
else
  sed=sed
fi

logName=`basename "$1"`
dir="./replayscripts/$logName"

if [ -d "$dir" ]; then
  rm -r "$dir"
fi
mkdir -p "$dir"

cat "$1" |
  grep '[0-9]\+ \(<\|>\)' |
  $sed 's|^\([0-9]\+\) > \([A-Z]\+\) '"$SERVERPREFIX"'\(.*\)|\1 > \2 \3 HTTP/1.1|' > ./tmp.log

max=$(cat "$1" | grep -o '[0-9]\+ \(<\|>\)' | sort -n -s | tail -n1 | grep -o '[0-9]\+')
size=$(echo $max | wc -c) #size now contains amount-of-digits-in-max + 1 because of the line ending

prevNum="1"
prefix=0
echo -n ""
while read line; do
  echo -en "\r$prevNum"
  if [[ $line =~ ^([0-9]*)\ (.)\ ?(.*)$ ]]; then
    number="${BASH_REMATCH[1]}"
    type="${BASH_REMATCH[2]}"
    data="${BASH_REMATCH[3]}"

    if [ $number = "1" -a $prevNum != "1" ]; then # numbering has restarted
      let prefix=$prefix+1
      echo -e "\r\033[Krestart after $prevNum"
    fi
    filename=$(printf "%q/%s%0${size}d" "$dir" "$prefix" "$number")
    if [ "$type" = '>' ]; then
      echo "$data" >> "$filename".request
    elif [ "$type" = '<' ]; then
      echo "$data" >> "$filename".expectedheaders
    else
      echo -e "\r\033[KError on $line"
    fi
    prevNum=$number
  else
    echo -e "\r\033[Kunmatching line: '$line'"
  fi
done < tmp.log
echo -en "\r\033[K"

mv tmp.log "$dir"/full.log
