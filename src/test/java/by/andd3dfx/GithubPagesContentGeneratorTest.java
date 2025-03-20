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
    private final String INPUT_FILE_NAME = TEST_RESOURCES_PATH + "input.txt";
    private final String TEMPLATE_FILE_NAME = TEST_RESOURCES_PATH + "template.html";
    private final String EXPECTED_OUTPUT_FILE_NAME = TEST_RESOURCES_PATH + "expected-output.html";

    private GithubPagesContentGenerator generator;

    @BeforeEach
    public void setUp() {
        generator = new GithubPagesContentGenerator();
    }

    @Test
    public void generateIntoString() throws IOException {
        var content = generator.generate(INPUT_FILE_NAME, TEMPLATE_FILE_NAME);

        String expectedContent = Files.readString(Path.of(EXPECTED_OUTPUT_FILE_NAME));
        assertThat(content, is(expectedContent));
    }

    @Test
    public void generateIntoFile() throws IOException {
        String outputFileName = "./target/expected-output.html";

        var content = generator.generate(INPUT_FILE_NAME, TEMPLATE_FILE_NAME, outputFileName);

        String expectedContent = Files.readString(Path.of(EXPECTED_OUTPUT_FILE_NAME));
        assertThat(content, is(expectedContent));
        checkGeneratedFileContent(outputFileName, EXPECTED_OUTPUT_FILE_NAME);
    }

    private void checkGeneratedFileContent(String generatedFileName, String expectedOutputFileName) throws IOException {
        Path generatedFilePath = Path.of(generatedFileName);
        String[] generatedFileLines = Files.readString(generatedFilePath).split("\n");

        Path expectedFilePath = Path.of(expectedOutputFileName);
        String[] expectedFileLines = Files.readString(expectedFilePath).split("\n");

        assertThat("Unexpected amount of lines in file " + generatedFilePath,
                generatedFileLines.length, is(expectedFileLines.length));

        for (int i = 0; i < generatedFileLines.length; i++) {
            assertThat("Wrong file content for file " + generatedFilePath,
                    generatedFileLines[i], is(expectedFileLines[i]));
        }
    }
}
