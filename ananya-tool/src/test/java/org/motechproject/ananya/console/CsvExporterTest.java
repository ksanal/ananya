package org.motechproject.ananya.console;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class CsvExporterTest {
    @Test
    public void shouldBuildCsvReportWithTheGivenArguments() throws IOException {
        String outputFileName = "outputFileName.csv";
        String filtersFile = "/filters.txt";
        String[] arguments = {"SampleFLW", filtersFile, outputFileName};

        CsvExporter.main(arguments);

        FileInputStream fileInputStream = new FileInputStream(outputFileName);
        String fileContent = IOUtils.toString(fileInputStream);

        assertEquals("Msisdn,Custom column name\n" +
                "1234,title\n", fileContent);
        FileUtils.deleteQuietly(new File(outputFileName));
    }
}


