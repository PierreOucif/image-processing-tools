package fr.ilysse.imageprocessing.data;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by p_poucif on 23/02/2017.
 */
public class CSVReader {

    public static List<DataToProcess> readDataFromCSV(final String csvFile, final String csvSeparator, final String sourceFolder) throws IOException {
        List<DataToProcess> dataToProcessList = new ArrayList<>();
        Path path = Paths.get(csvFile);
        if (Files.notExists(path)) {
            return Collections.emptyList();
        }

        String separator = Pattern.quote(csvSeparator);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path.toFile()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(separator);
                if (data.length == 3) {
                    dataToProcessList.addAll(toDataToProcessJustImageName(data, sourceFolder));
                }
            }
        } finally {
            br.close();
            return dataToProcessList;
        }
    }

    public static List<DataToProcess> toDataToProcessJustImageName(String[] data, String sourceFolder) throws IOException {
        final String fileName = data[0];
        final List<Path> fileNameFromArchive = getFileNameFromArchive(fileName, sourceFolder);
        final List<DataToProcess> dataToProcesses = Lists.newArrayList();
        for (Path path : fileNameFromArchive) {
            dataToProcesses.add(new DataToProcess(path.getFileName().toString(), data[2], Template.F9));
        }
        return dataToProcesses;
    }

    public static DataToProcess toDataToProcess(String[] line) throws Exception {
        for (Template template : Template.values()) {
            if (line[2].equals(template.getCode())) {
                return new DataToProcess(line[0], line[1], Template.F9);
            }
        }
        throw new Exception("No data found");
    }

    public static List<Path> getFileNameFromArchive(String ugArtice, String sourceFolder) throws IOException {
        List<Path> collect = Files.list(Paths.get(sourceFolder))
                .filter(file -> file.getFileName().toString().contains(ugArtice))
                .collect(Collectors.toList());
        return collect;
    }

}
