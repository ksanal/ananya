package org.motechproject.ananya.performance.data;

import org.motechproject.ananya.performance.framework.PerformanceData;
import org.motechproject.ananya.repository.AllNodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class CertificateCoursePostDataSetup {

    public static final int CHAPTERS_COUNT = 9;

    @Autowired
    private AllNodes allNodes;

    @PerformanceData(testName = "certificate_course-content", description = "CertificateCourse call-flow javascript generation")
    public void setupAllDataPostsWithContentIds() throws IOException {
        createAllDataPostsFile();
        createCourseRootFile();
        for (int i = 1; i <= CHAPTERS_COUNT; i++)
            createCourseChapterFile("certificate_course_chapter" + i + ".js", i);

    }

    private void createCourseRootFile() throws IOException {
        String rootJson = allNodes.nodeWithoutChildrenAsJson("CertificationCourse");
        String filePath = "jmeter/js/certificate_course_without_levels.js";
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        writer.write(String.format("vars.putObject('course_data', %s)", rootJson));
        writer.close();
    }

    private void createAllDataPostsFile() throws IOException {
        String templateFile = "jmeter/js/all_data_posts_template.js";
        BufferedReader templateReader = new BufferedReader(new FileReader(templateFile));

        String allDataPostsFile = "jmeter/js/all_data_posts_jmeter.js";
        BufferedWriter allDataPostsWriter = new BufferedWriter(new FileWriter(allDataPostsFile));

        String line = templateReader.readLine();
        while (line != null) {
            allDataPostsWriter.newLine();
            allDataPostsWriter.write(line);
            line = templateReader.readLine();
        }
        allDataPostsWriter.close();
        templateReader.close();
    }

    private void createCourseChapterFile(String chapterJsFile, int chapterNumber) throws IOException {
        String json = allNodes.nodeAsJson("Chapter " + chapterNumber);
        String filePath = "jmeter/js/" + chapterJsFile;
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        writer.write(String.format("vars.getObject('course_data').children.push(%s);", json));
        writer.close();
    }

}
