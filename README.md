# Cabo Amazon Crawler

An Amazon product data web crawler written in Clojure

## Description

When run the program takes in a list of search terms from the resources folder to use to search Amazon for.
It generates hundreds of search URLs and programtically downloads the product links from each search results page in parallel.
It then adds these product links to an in memory queue so that the product data and images can be downloaded in paralell.
When product data is finished downloading it is added into a local database that is running using docker compose.
Paralellism is dynamically configured per your machine type in core.clj.

## Clojure concepts used

    - Multithreading
    - In Memory Queues
    - Atoms
    - Threading Macros
    - Configurability
    - Sets
    - Maps
    - Vectors
    - Map/Filter/Reduce functions
    - HoneySQL

## Other concepts used

    - HTTP
    - Retries
    - Handling Blocks
    - Proxy services
    - Postgres
    - SQL
    - Duplicate Detection and Avoidance
    - Web Crawling
    - Parallelism