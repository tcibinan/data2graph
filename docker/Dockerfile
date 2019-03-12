# Build stage
FROM ubuntu:18.04 as builder

ENV DATA2GRAPH_HOME=/opt/data2graph
ENV NODEJS_HOME=/opt/nodejs

# Java 8
RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-8-jre openjdk-8-jdk \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Utils
RUN apt-get update \
    && apt-get install -y --no-install-recommends wget xz-utils \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Nodejs
RUN mkdir -p $NODEJS_HOME \
    && cd $NODEJS_HOME \
    && wget https://nodejs.org/dist/v10.15.1/node-v10.15.1-linux-x64.tar.xz \
    && tar xf node-v10.15.1-linux-x64.tar.xz \
    && cp -r node-v10.15.1-linux-x64/* ./ \
    && rm -r node-v10.15.1-linux-x64 \
    && echo "export NODEJS_HOME=$NODEJS_HOME" >> /etc/profile.d/nodejs.sh \
    && echo "export PATH=\$NODEJS_HOME/bin:\$PATH" >> /etc/profile.d/nodejs.sh \
    && chmod +x /etc/profile.d/nodejs.sh

# Data2Graph
ARG TAG

RUN wget https://github.com/tcibinan/data2graph/archive/$TAG.tar.gz -O data2graph-$TAG.tar.gz \
    && mkdir -p $DATA2GRAPH_HOME \
    && tar -xzf data2graph-$TAG.tar.gz -C $DATA2GRAPH_HOME \
    && rm data2graph-$TAG.tar.gz \
    && cp -r $DATA2GRAPH_HOME/data2graph-${TAG#v}/* $DATA2GRAPH_HOME/ \
    && rm -r $DATA2GRAPH_HOME/data2graph-${TAG#v} \
    && . /etc/profile.d/nodejs.sh \
    && $DATA2GRAPH_HOME/gradlew assembleFrontend -p $DATA2GRAPH_HOME

# Production stage
FROM ubuntu:18.04

ENV DATA2GRAPH_HOME=/opt/data2graph

WORKDIR $DATA2GRAPH_HOME

# Nginx
RUN apt-get update \
    && apt-get install -y --no-install-recommends nginx \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && rm /etc/nginx/sites-enabled/default \
    && rm /etc/nginx/sites-available/default

COPY ./data2graph.nginx.conf /etc/nginx/sites-enabled/data2graph.nginx.conf

COPY --from=builder $DATA2GRAPH_HOME/build/dist .

CMD nginx && sleep infinity
