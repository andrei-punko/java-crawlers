
# Collection of crawlers for several sites

[![Java CI with Maven](https://github.com/andrei-punko/java-crawlers/actions/workflows/maven.yml/badge.svg)](https://github.com/andrei-punko/java-crawlers/actions/workflows/maven.yml)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/andrei-punko/java-crawlers/actions/workflows/maven.yml)
[![Branches](.github/badges/branches.svg)](https://github.com/andrei-punko/java-crawlers/actions/workflows/maven.yml)

- [1k.by](http://1k.by) crawler

  See [PriceSearchUtilTest](all-in-one/src/test/java/by/andd3dfx/sitesparsing/firstcatalog/PriceSearchUtilTest.java)


- [onliner.by](http://onliner.by) crawler

  See [CpuSearchUtilTest](all-in-one/src/test/java/by/andd3dfx/sitesparsing/onlinerby/CpuSearchUtilTest.java)


- [rabota.by](http://rabota.by) crawler

  See [StatisticsTest](all-in-one/src/test/java/by/andd3dfx/sitesparsing/tutby/StatisticsTest.java)
  and [TutByJobSearchUtilTest](all-in-one/src/test/java/by/andd3dfx/sitesparsing/tutby/TutByJobSearchUtilTest.java)


- [pravtor.ru](http://pravtor.ru) crawler

  See [SearchUtilTest](pravtor-search/src/test/java/by/andd3dfx/pravtor/util/SearchUtilTest.java)
  and [FileUtilTest](pravtor-search/src/test/java/by/andd3dfx/pravtor/util/FileUtilTest.java)

Decided to keep all of them in one repo to make other repos free from making calls to external resources
