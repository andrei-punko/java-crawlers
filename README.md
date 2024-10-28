
# Collection of Java-based web crawlers
[![Java CI with Maven](https://github.com/andrei-punko/java-crawlers/actions/workflows/maven.yml/badge.svg)](https://github.com/andrei-punko/java-crawlers/actions/workflows/maven.yml)

## Prerequisites

- Maven 3
- JDK 21

## How to build
```
mvn clean install
```

## Crawler for Orthodox torrent tracker [pravtor.ru](http://pravtor.ru)
Check [SearchUtilTest](pravtor.ru-crawler/src/test/java/by/andd3dfx/pravtor/util/SearchUtilTest.java)
and [FileUtilTest](pravtor.ru-crawler/src/test/java/by/andd3dfx/pravtor/util/FileUtilTest.java) for details.

To make search - run [run-search.bat](pravtor.ru-crawler/run-search.bat) script.  
Collected data will be placed into [result.xls](pravtor.ru-crawler/sandbox/result.xls) file in `sandbox` folder

## Crawler for vacancies aggregator [rabota.by / hh.ru](http://rabota.by)
Check [RabotaByJobSearchUtil](rabota.by-crawler/src/main/java/by/andd3dfx/sitesparsing/rabotaby/RabotaByJobSearchUtil.java) for details.

To make search - run `main()` method of [MainApp](rabota.by-crawler/src/main/java/by/andd3dfx/sitesparsing/rabotaby/MainApp.java) class
