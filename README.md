
# Collection of crawlers for several sites

[![Java CI with Maven](https://github.com/andrei-punko/java-crawlers/actions/workflows/maven.yml/badge.svg)](https://github.com/andrei-punko/java-crawlers/actions/workflows/maven.yml)

- [rabota.by](http://rabota.by) crawler

  See [StatisticsTest](all-in-one/src/test/java/by/andd3dfx/sitesparsing/rabotaby/StatisticsTest.java)
  and [RabotaByJobSearchUtilTest](all-in-one/src/test/java/by/andd3dfx/sitesparsing/rabotaby/RabotaByJobSearchUtilTest.java)


- [pravtor.ru](http://pravtor.ru) crawler

  See [SearchUtilTest](pravtor-search/src/test/java/by/andd3dfx/pravtor/util/SearchUtilTest.java)
  and [FileUtilTest](pravtor-search/src/test/java/by/andd3dfx/pravtor/util/FileUtilTest.java)

Decided to keep all of them in one repo to make other repos free from making calls to external resources
