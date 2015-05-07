#!/bin/sh
file=$1
Array=($(busybox awk '{print $1}' $file))
for i in ${Array[@]}
do
        key=$(echo $i|busybox awk -F '=' '{print $1}')
        if [ "$key" != "ro.hardware" ];then
                value=$(echo $i|busybox awk -F '=' '{print $2}')
                setprop $key $value
        fi
done
