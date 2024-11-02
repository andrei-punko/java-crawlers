
# Collection of Java-based web crawlers
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
Check [SearchUtil](pravtor.ru-crawler/src/main/java/by/andd3dfx/pravtor/util/SearchUtil.java) 
in `pravtor.ru-crawler` module for details

To make search - run [run-search.bat](pravtor.ru-crawler/run-search.bat) script.  
Collected data will be placed into [result.xls](pravtor.ru-crawler/sandbox/result.xls) file in `sandbox` folder

## Crawler for vacancies aggregator [rabota.by / hh.ru](http://rabota.by)
Check [SearchUtil](rabota.by-crawler/src/main/java/by/andd3dfx/sitesparsing/rabotaby/SearchUtil.java) 
in `rabota.by-crawler` module for details

To make search - run `main()` method of [MainApp](rabota.by-crawler/src/main/java/by/andd3dfx/sitesparsing/rabotaby/MainApp.java) 
class with populated output path in command line param
