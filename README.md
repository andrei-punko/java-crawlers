# Collection of Java-based Web crawlers

[![Java CI with Maven](https://github.com/andrei-punko/java-crawlers/actions/workflows/maven.yml/badge.svg)](https://github.com/andrei-punko/java-crawlers/actions/workflows/maven.yml)

## Prerequisites

- Maven 3
- JDK 21

## How to build

```
mvn clean install
```

## Common crawler functionality

- Your crawler should extend [WebCrawler](crawler-engine/src/main/java/by/andd3dfx/crawler/engine/WebCrawler.java)
  base crawler class
- DTO class which describes collected data should implement
  [CrawlerData](crawler-engine/src/main/java/by/andd3dfx/crawler/dto/CrawlerData.java) marker interface

## Crawler for Orthodox torrent tracker [pravtor.ru](http://pravtor.ru)

Check [PravtorRuWebCrawler](pravtor.ru-crawler/src/main/java/by/andd3dfx/pravtor/crawler/PravtorRuWebCrawler.java) for
details

To make search - use [run-search](pravtor.ru-crawler/run-search.bat) script in [pravtor.ru-crawler folder](pravtor.ru-crawler).  
Collected data will be placed into [result.xls](pravtor.ru-crawler/sandbox/result.xls) file in [sandbox folder](pravtor.ru-crawler/sandbox)

## Crawler for vacancies aggregator [rabota.by](http://rabota.by)

Check [RabotaByWebCrawler](rabota.by-crawler/src/main/java/by/andd3dfx/rabotaby/crawler/RabotaByWebCrawler.java) for
details

To make search - use [run-search](rabota.by-crawler/run-search.bat) script in [rabota.by-crawler folder](rabota.by-crawler).

## Video with description of the project

[![YouTube link](https://markdown-videos-api.jorgenkh.no/url?url=https%3A%2F%2Fyoutu.be%2F4qxvkALcWjQ)](https://youtu.be/4qxvkALcWjQ)
