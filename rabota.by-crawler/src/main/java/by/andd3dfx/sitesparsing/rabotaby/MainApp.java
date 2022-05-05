package by.andd3dfx.sitesparsing.rabotaby;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

public class MainApp {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Path to output file should be populated!");
        }
        var searchUtil = new RabotaByJobSearchUtil();

        LinkedHashMap<String, Integer> statisticsSortedMap = searchUtil.collectStatistics("java");
        Path path = Paths.get(args[0]);
        byte[] strToBytes = statisticsSortedMap.toString().getBytes();
        Files.write(path, strToBytes);
    }
}
