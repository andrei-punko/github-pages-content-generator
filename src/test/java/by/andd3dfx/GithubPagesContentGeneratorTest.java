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
    public void parse() throws IOException {
        String inputFileName = "./src/test/resources/existing/input.txt";
        String outputFileName = "./target/output.txt";
        String expectedOutputFileName = "./src/test/resources/expected/output.txt";

        parser.parse(inputFileName, outputFileName);

        checkGeneratedFileContent(expectedOutputFileName);
    }

    @Test
    public void generateIndexHtml() throws IOException {
        String expectedOutputFileName = "./src/test/resources/expected/index.html";

        parser.generateIndexHtml("./target/output.txt", "./src/main/resources/template.html", "./target/index.html");

        checkGeneratedFileContent(expectedOutputFileName);
    }

    private void checkGeneratedFileContent(String expectedOutputFileName) throws IOException {
        Path expectedFilePath = Path.of(expectedOutputFileName);
        Path generatedFilePath = Path.of("./target", expectedFilePath.getFileName().toString());
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
