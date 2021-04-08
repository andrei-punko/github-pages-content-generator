package by.andd3dfx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Stack;

/**
 * Usage example:
 * java -jar github-pages-parser.jar inputFileName templateFileName htmlOutputFileName
 */
public class GithubPagesContentGenerator {

    private static final String PLACEHOLDER_STRING = "***CONTENT_PLACEHOLDER***";

    public void parse(String inputFileName, String outputFileName) {
        try (
                BufferedReader reader = new BufferedReader(new FileReader(inputFileName, StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName, StandardCharsets.UTF_8));
        ) {
            String line;
            String pBuffer = "";

            Stack<String> bulletedListStartedFlagsStack = new Stack<>();

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    // Line is blank
                    if (pBuffer.isBlank()) {
                        // Buffer is empty
                        pBuffer += "\n";
                        continue;
                    }
                    continue;
                }

                // Line is not blank
                if (line.startsWith("* ") || line.startsWith("- ")) {
                    while (!bulletedListStartedFlagsStack.isEmpty()) {
                        pBuffer += "</ul>\n";
                        bulletedListStartedFlagsStack.pop();
                    }

                    if (!pBuffer.isEmpty()) {
                        // Buffer is not empty
                        writer.write(wrapWitnP(pBuffer) + "\n");
                        pBuffer = "";
                    }

                    // Title line
                    pBuffer += wrapWithB(line) + "<br/>\n";
                    continue;
                }

                if (line.startsWith("```")) {
                    // Start of PRE block
                    String preBuffer = "";
                    boolean preStarted = true;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("```")) {
                            // End of PRE block
                            pBuffer += wrapWithPre(preBuffer);
                            preStarted = false;
                            break;
                        }

                        preBuffer += line + "\n";
                    }
                    if (preStarted) {
                        throw new IllegalStateException("Ending '```' was not found!");
                    }
                    continue;
                }

                // Usual line
                line = line.trim();
                if (line.matches("^=+\\s.*")) {
                    String startingBulletedPart = line.substring(0, line.indexOf(" "));
                    if (bulletedListStartedFlagsStack.isEmpty()) {
                        pBuffer += "<ul>\n";
                        bulletedListStartedFlagsStack.push(startingBulletedPart);
                    } else {
                        String topStackElement = bulletedListStartedFlagsStack.peek();
                        if (!startingBulletedPart.equals(topStackElement)) {
                            pBuffer += "<ul>\n";
                            bulletedListStartedFlagsStack.push(startingBulletedPart);
                        }
                    }
                    line = line.replaceFirst("^=+\\s", "");
                    pBuffer += wrapWithLi(capitalize(line));
                    continue;
                }

                pBuffer += capitalize(line) + "<br/>" + "\n";
                continue;
            }

            // Dump pBuffer content if it isn't empty
            if (!pBuffer.isBlank()) {
                while (!bulletedListStartedFlagsStack.isEmpty()) {
                    pBuffer += "</ul>\n";
                    bulletedListStartedFlagsStack.pop();
                }
                writer.write(wrapWitnP(pBuffer));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private String wrapWithUl(String str) {
        return "<ul>\n" + str + "\n</ul>\n";
    }

    private String wrapWithLi(String str) {
        return "<li>" + str + "</li>\n";
    }

    private String wrapWithPre(String buffer) {
        buffer = buffer
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
        return "<pre>\n" + buffer + "</pre>\n";
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

        GithubPagesContentGenerator parser = new GithubPagesContentGenerator();
        parser.parse(inputFileName, tmpFileName);
        parser.generateIndexHtml(tmpFileName, templateFileName, htmlOutputFileName);

        Files.delete(Path.of(tmpFileName));
    }
}
