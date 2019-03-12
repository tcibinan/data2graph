#!/usr/bin/env bash

set -e

if [[ $# -eq 0 ]] ; then
    echo 'data2graph image tag should be specified as an argument'
    exit 1
fi

tag=$1

docker build . -t flaxo/data2graph:$tag \
               --network=host \
               --build-arg TAG=v$tag
