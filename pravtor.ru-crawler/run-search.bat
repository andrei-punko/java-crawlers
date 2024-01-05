
cd ./sandbox
del result.xls
call java -jar ../target/pravtor.ru-crawler-1.0-SNAPSHOT-jar-with-dependencies.jar params.txt result.xls
cd ..
