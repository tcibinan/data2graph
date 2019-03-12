[![from_flaxo with_♥](https://img.shields.io/badge/from_flaxo-with_♥-blue.svg)](https://github.com/tcibinan/flaxo)
[![Docker Cloud Build Status Data2Graph](https://img.shields.io/docker/cloud/build/flaxo/data2graph.svg?label=docker%20build)](https://hub.docker.com/r/flaxo/data2graph)

# data2graph

data2graph is a Kotlin-JS web application that helps visualize graphs in a force-directed manner. It is a 
[data2viz](https://github.com/data2viz/data2viz) port of the original 
[d3.js force-directed graph](https://beta.observablehq.com/@mbostock/d3-force-directed-graph).

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

## Custom graph

Data2Viz can be used to visualize any properly formatted graph json that is accessible by `GET` endpoint. To show 
custom graph just add postfix with your graph json endpoint: 

```
http://localhost:8088/?graph_url=http://yourendpoint.com/data.json
```

## Demo

Demo shows graph of an anonymized plagiarism report that was collected for an actual university programming course.

![plagiarism graph](docs/images/plagiarism_graph.png?raw=true)

## Building

To start the development server run the command and open the browser at [http://localhost:8088/](http://localhost:8088/)

```bash
./gradlew run
```

To stop the development server run the following command

```bash
./gradlew stop
```
