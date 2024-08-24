
# Collection of web crawlers
[![Java CI with Maven](https://github.com/andrei-punko/java-crawlers/actions/workflows/maven.yml/badge.svg)](https://github.com/andrei-punko/java-crawlers/actions/workflows/maven.yml)

Decided to keep all of them in one repo to make other repos free from making calls to external resources

## Prerequisites

- Maven 3
- JDK 17

## How to build
```
mvn clean install
```

## Crawlers

### The [pravtor.ru](http://pravtor.ru) crawler
  See [SearchUtilTest](pravtor.ru-crawler/src/test/java/by/andd3dfx/pravtor/util/SearchUtilTest.java)
  and [FileUtilTest](pravtor.ru-crawler/src/test/java/by/andd3dfx/pravtor/util/FileUtilTest.java)

### How to use
```
./pravtor.ru-crawler/search.bat
```
