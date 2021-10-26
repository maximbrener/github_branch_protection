package com.onms.gh;

import com.onms.gh.dto.Branch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    public static void printCSV(Map<String, List<Branch>> reposBranchesMap, String fileName) {
        List<String[]> dataLines = new ArrayList<>();
        for (String repo : reposBranchesMap.keySet()) {
            List<String> branches = reposBranchesMap.get(repo).stream()
                    .map(Branch::getName)
                    .collect(Collectors.toList());
            String[] result = Arrays.copyOf(new String[]{repo}, 1 + reposBranchesMap.get(repo).size());
            System.arraycopy(branches.toArray(), 0, result, 1, reposBranchesMap.get(repo).size());
            dataLines.add(result);
        }
        File csvOutputFile = new File(fileName);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            dataLines.stream()
                    .map(Utils::convertToCSV)
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(Utils::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    public static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public static Set<String> listFiles(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());
    }

    public static String readFile(String dir, String file) throws IOException {
        return Files.readString(Path.of(dir, file));
    }
}
