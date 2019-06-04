#!/bin/sh

set -x

COMMON_JAVA_ARGS="-Dcom.sun.management.jmxremote.rmi.port=9091 \
-Dcom.sun.management.jmxremote=true \
-Dcom.sun.management.jmxremote.port=9091 \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.local.only=false"
'-Djava.rmi.server.hostname=robotcontroller'

function cleanup() {
    set +e
    # TODO: better container management
    for i in robotshop robotcontroller robotmaker; do
        docker kill "$(docker ps -a -q --filter ancestor=andrewazores/$i)"
        docker rm "$(docker ps -a -q --filter ancestor=andrewazores/$i)"
    done
}

cleanup
trap cleanup EXIT

set +e
docker network create --attachable robot-demo
set -e

for i in robotmaker robotcontroller robotshop; do
    docker run \
        --net robot-demo \
        --name $i \
        --memory 80M \
        -P \
        --rm -d "andrewazores/$i"
done

docker run \
    --net robot-demo \
    --name jmx-client \
    --memory 80M \
    -p 8080:8080 \
    --rm -it andrewazores/container-jmx-client "$@"
