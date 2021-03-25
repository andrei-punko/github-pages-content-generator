package by.andd3dfx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Usage example:
 * java -jar github-pages-parser.jar inputFileName templateFileName htmlOutputFileName
 */
public class GithubPagesParser {

    private static final String PLACEHOLDER_STRING = "***CONTENT_PLACEHOLDER***";

    public void parse(String inputFileName, String outputFileName) {
        try (
                BufferedReader reader = new BufferedReader(new FileReader(inputFileName, StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName, StandardCharsets.UTF_8));
        ) {
            String line;
            String pBuffer = "";
            String zBuffer = "";
            boolean startedCodeBlock = false;
            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    // line is blank
                    if (pBuffer.isBlank() || startedCodeBlock) {
                        pBuffer += "\n";
                        continue;
                    }

                    writer.write(wrapWitnP(pBuffer) + "\n");
                    pBuffer = "";
                    continue;
                }

                // line is not blank
                if (line.startsWith("* ") || line.startsWith("- ")) {
                    pBuffer += wrapWithB(line) + "<br/>\n";
                    continue;
                }

                if (line.startsWith("```")) {
                    pBuffer += (startedCodeBlock ? "</pre>" : "<pre>") + "\n";
                    startedCodeBlock = !startedCodeBlock;
                    continue;
                }

                if (!startedCodeBlock) {
                    line = capitalize(line) + "<br/>";
                }
                pBuffer += line + "\n";
                continue;
            }

            if (startedCodeBlock) {
                throw new IllegalStateException("Ending '```' was not found!");
            }

            // Dump pBuffer content if it isn't empty
            if (!pBuffer.isBlank()) {
                writer.write(wrapWitnP(pBuffer));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private String wrapWithB(String line) {
        return "<b>" + capitalize(line.substring(2)) + "</b>";
    }

    private String wrapWitnP(String buffer) {
        return "<p align=\"justify\">\n" + buffer + "</p>";
    }

    private String capitalize(String str) {
        if (str.startsWith("http://") || str.startsWith("https://")) {
            return "<a href=\"" + str + "\">" + str + "</a>";
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public void generateIndexHtml(String contentFileName, String templateFileName, String outputFileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName, StandardCharsets.UTF_8));) {
            String templateContent = Files.readString(Path.of(templateFileName));
            String content = Files.readString(Path.of(contentFileName));
            String indexHtmlContent = templateContent.replace(PLACEHOLDER_STRING, content);

            writer.write(indexHtmlContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            throw new IllegalArgumentException("Wrong amount of incoming params: inputFileName, templateFileName and htmlOutputFileName should be populated!");
        }

        String inputFileName = args[0];
        String tmpFileName = "tmp.txt";
        String templateFileName = args[1];
        String htmlOutputFileName = args[2];

        GithubPagesParser parser = new GithubPagesParser();
        parser.parse(inputFileName, tmpFileName);
        parser.generateIndexHtml(tmpFileName, templateFileName, htmlOutputFileName);

        Files.delete(Path.of(tmpFileName));
    }
}
