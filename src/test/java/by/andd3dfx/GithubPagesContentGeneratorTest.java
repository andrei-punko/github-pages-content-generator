package by.andd3dfx;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GithubPagesContentGeneratorTest {

    private GithubPagesContentGenerator parser;

    @Before
    public void setUp() {
        parser = new GithubPagesContentGenerator();
    }

    @Test
    public void generate() throws IOException {
        String inputFileName = "./src/test/resources/input.txt";
        String templateFileName = "./src/test/resources/template.html";
        String expectedGeneratedContent = Files.readString(Path.of("./src/test/resources/output.html"));

        String content = parser.generate(inputFileName, templateFileName);

        assertThat(content, is(expectedGeneratedContent));
    }

    @Test
    public void generateByTemplate() throws IOException {
        String inputFileName = "./src/test/resources/input.txt";
        String templateFileName = "./src/test/resources/template.html";
        String expectedOutputFileName = "./src/test/resources/output.html";
        String outputFileName = "./target/output.html";

        parser.generate(inputFileName, templateFileName, outputFileName);

        checkGeneratedFileContent(outputFileName, expectedOutputFileName);
    }

    private void checkGeneratedFileContent(String generatedFileName, String expectedOutputFileName) throws IOException {
        Path generatedFilePath = Path.of(generatedFileName);
        Path expectedFilePath = Path.of(expectedOutputFileName);
        String generatedFileContent = Files.readString(generatedFilePath);
        String expectedFileContent = Files.readString(expectedFilePath);

        String[] generatedFileLines = generatedFileContent.split("\n");
        String[] expectedFileLines = expectedFileContent.split("\n");

        assertThat("Unexpected amount of lines in file " + generatedFilePath, generatedFileLines.length,
                is(expectedFileLines.length));

        for (int i = 0; i < generatedFileLines.length; i++) {
            assertThat("Wrong file content for file " + generatedFilePath,
                    generatedFileLines[i], is(expectedFileLines[i]));
        }
    }
}
