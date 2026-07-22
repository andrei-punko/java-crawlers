@echo off
chcp 65001 >nul
rem Resume mode: keep profiles.json and photos/, skip already known ids
rem args: outputJson [pagesCap] [delayMs] [minAge] [maxAge]
call java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar target/tabor.ru-crawler-1.0-SNAPSHOT-jar-with-dependencies.jar profiles.json -1 250 30 45
