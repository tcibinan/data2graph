[![from_flaxo with_♥](https://img.shields.io/badge/from_flaxo-with_♥-blue.svg)](https://github.com/tcibinan/flaxo)
[![Docker Cloud Build Status Data2Graph](https://img.shields.io/docker/cloud/build/flaxo/data2graph.svg?label=docker%20build)](https://hub.docker.com/r/flaxo/data2graph)

# data2graph

data2graph is a Kotlin-JS web application that helps visualize graphs in a force-directed manner. It is a 
[data2viz](https://github.com/data2viz/data2viz) port of the original 
[d3.js force-directed graph](https://beta.observablehq.com/@mbostock/d3-force-directed-graph).

## Demo

Demo shows graph of an anonymized plagiarism report that was collected for an actual university programming course.

![plagiarism graph](https://github.com/tcibinan/data2graph/raw/master/docs/images/plagiarism_graph.png)

## Controls

Tool has four configurable preferences:
- Plagiarism weight **threshold**.
- Graph links absolute **shift**.
- Graph links relative **scale**.
- Plagiarism weight **normalization** strategy which has 3 available values:
   - **disabled** normalization which basically means that weights are just scaled by 100,
   - **max** normalization which means that weights are scaled by the maximum weight,
   - **collapsing** normalization that normalizes weights as a binary function: all weights
     that are above the set threshold are scaled to 1 and 0 otherwise.

## Visualize your data

Data2Viz can be used to visualize any graph json which satisfies the following requirements:

   1. Graph json should be formatted according to this [demo graph json](https://tcibinan.github.io/data2graph/latest/data.json).   
   2. Graph json should be accessible by some endpoint.
   3. Graph json endpoint should allow `https://tcibinan.github.io` origin. See [CORS configuration](https://developer.mozilla.org/ru/docs/Web/HTTP/CORS).
   4. Graph json endpoint should be encoded. See [URL encoding](https://www.w3schools.com/tags/ref_urlencode.ASP).

To show your graph json just add `graph_url` with your endpoint to data2graph url

```
https://tcibinan.github.io/data2graph/latest/?graph_url=http%3A%2F%2Fyourendpoint.com%2Fdata.json
```

## Local deployment

You can also start your own instance of data2graph locally

```bash
docker run --rm --name data2graph -d -p 8080:80 flaxo/data2graph:0.4
```

To show a custom graph with your encoded graph json url

```
http://localhost:8088/?graph_url=http%3A%2F%2Fyourendpoint.com%2Fdata.json
```

Or you can copy your `graph.json` to the data2graph container

```bash
docker cp graph.json data2graph:/opt/data2graph/graph.json
```

And show your graph using relative url

```bash
http://localhost:8088/?graph_url=graph.json
```

## Contribution

To start the development server run the command and open the browser at [http://localhost:8088/](http://localhost:8088/)

```bash
./gradlew run
```

To stop the development server run the following command

```bash
./gradlew stop
```
