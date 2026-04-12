@echo off
REM Сборка: mvn -pl onliner.by-crawler -am package
REM Второй аргумент: лимит страниц выдачи (по ~30 товаров). -1 = все страницы.
java -jar target\onliner.by-crawler-1.0-SNAPSHOT-jar-with-dependencies.jar processors-am4.json -1 300
