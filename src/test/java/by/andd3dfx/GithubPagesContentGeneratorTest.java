package by.andd3dfx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GithubPagesContentGeneratorTest {

    private final String TEST_RESOURCES_PATH = "./src/test/resources/";

    private GithubPagesContentGenerator generator;

    @BeforeEach
    public void setUp() {
        generator = new GithubPagesContentGenerator();
    }

    @Test
    public void generateIntoFile() throws IOException {
        final String INPUT_FILE_NAME = TEST_RESOURCES_PATH + "input.txt";
        final String TEMPLATE_FILE_NAME = TEST_RESOURCES_PATH + "template.html";
        final String OUTPUT_FILE_NAME = "./target/output.html";

        generator.generate(INPUT_FILE_NAME, TEMPLATE_FILE_NAME, OUTPUT_FILE_NAME);

        String EXPECTED_OUTPUT_FILE_NAME = TEST_RESOURCES_PATH + "expected-output.html";
        checkGeneratedFileContent(OUTPUT_FILE_NAME, EXPECTED_OUTPUT_FILE_NAME);
    }

    private void checkGeneratedFileContent(String generatedFileName, String expectedFileName) throws IOException {
        Path generatedFilePath = Path.of(generatedFileName);
        String[] generatedFileLines = Files.readString(generatedFilePath).split("\n");

        Path expectedFilePath = Path.of(expectedFileName);
        String[] expectedFileLines = Files.readString(expectedFilePath).split("\n");

        System.out.println("Expected lines: " + expectedFileLines.length + "\n" +
                "Generated lines: " + generatedFileLines.length);

        for (int i = 0; i < generatedFileLines.length; i++) {
            assertThat("Wrong file content for file " + generatedFilePath,
                    generatedFileLines[i], is(expectedFileLines[i]));
        }
    }
}
