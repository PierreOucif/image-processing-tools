#!/bin/bash
CSV_FILE=$1
DIR_TARGET_BEG=$2

cat $CSV_FILE | while read line; do
                URL_IMG=`echo $line | cut -d';' -f2`
                DIR_TARGET=`echo $URL_IMG | cut -d'/' -f6`
                NAME_IMG=`echo $URL_IMG | cut -d'/' -f7`
                DIR_TARGET_2=`echo $DIR_TARGET | cut -c'1-3'`
                #echo "$DIR_TARGET_BEG/$DIR_TARGET_2/$DIR_TARGET/$NAME_IMG"
                #echo "$URL_IMG"
                mkdir -p $DIR_TARGET_BEG/$DIR_TARGET_2/$DIR_TARGET
                curl $URL_IMG > $DIR_TARGET_BEG/$DIR_TARGET_2/$DIR_TARGET/$NAME_IMG
done;
