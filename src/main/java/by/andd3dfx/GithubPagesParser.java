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
 * java -jar github-parser-1.0-SNAPSHOT.jar inputFileName templateFileName htmlOutputFileName
 */
public class GithubPagesParser {

    public void parse(String inputFileName, String outputFileName) {
        try (
                BufferedReader reader = new BufferedReader(new FileReader(inputFileName, StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName, StandardCharsets.UTF_8));
        ) {
            String line = null;
            String buffer = "";
            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    // Line os blank
                    if (buffer.isBlank()) {
                        writer.write("\n");
                    } else {
                        writer.write("<p align=\"justify\">\n" + buffer + "</p>\n");
                        buffer = "";
                        continue;
                    }
                } else {
                    // line is not blank
                    if (line.startsWith("* ") || line.startsWith("- ")) {
                        line = "<b>" + line.substring(2) + "</b>";
                    }

                    buffer += line + "</br>\n";
                    continue;
                }
            }

            // Dump buffer content if it isn't empty
            if (!buffer.isBlank()) {
                writer.write("<p align=\"justify\">\n" + buffer + "</p>\n");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void generateIndexHtml(String contentFileName, String templateFileName, String outputFileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName, StandardCharsets.UTF_8));) {
            String templateContent = Files.readString(Path.of(templateFileName));
            String content = Files.readString(Path.of(contentFileName));
            String indexHtmlContent = templateContent.replace("***CONTENT_PLACEHOLDER***", content);

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
