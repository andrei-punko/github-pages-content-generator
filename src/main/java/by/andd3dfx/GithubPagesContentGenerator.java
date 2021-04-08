package by.andd3dfx;

import java.io.*;
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

    public String generate(String inputFileName, String templateFileName) {
        StringBuilder outputBuffer = new StringBuilder();
        try (
                BufferedReader inputFileReader = new BufferedReader(new FileReader(inputFileName, StandardCharsets.UTF_8));
        ) {
            String line;
            StringBuilder pBuffer = new StringBuilder();
            Stack<String> bulletedListStack = new Stack<>();

            while ((line = inputFileReader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                // Title of new block
                if (line.startsWith("* ") || line.startsWith("- ")) {
                    // End all started bulleted lists if needed
                    while (!bulletedListStack.isEmpty()) {
                        pBuffer.append("</ul>\n");
                        bulletedListStack.pop();
                    }

                    // Dump current buffer content into output file
                    if (pBuffer.length() > 0) {
                        outputBuffer.append(wrapWitnP(pBuffer.toString()));
                        pBuffer.setLength(0);
                    }

                    // Title line
                    pBuffer.append(wrapWithB(line.substring(2)));
                    continue;
                }

                // Start of PRE block
                if (line.startsWith("```")) {
                    processPreBlock(inputFileReader, pBuffer);
                    continue;
                }

                // Remove starting/ending spaces
                line = line.trim();

                // Bulleted list item
                if (line.matches("^=+\\s.*")) {
                    String startingBulletedPart = line.substring(0, line.indexOf(" "));
                    if (bulletedListStack.isEmpty()) {
                        pBuffer.append("<ul>\n");
                        bulletedListStack.push(startingBulletedPart);
                    } else {
                        String topStackElement = bulletedListStack.peek();
                        if (!startingBulletedPart.equals(topStackElement)) {
                            pBuffer.append("<ul>\n");
                            bulletedListStack.push(startingBulletedPart);
                        }
                    }
                    // Remove bulleted item marker from line
                    line = line.replaceFirst("^=+\\s", "");
                    pBuffer.append(wrapWithLi(line));
                    continue;
                }

                // Usual line: just write it capitalized
                pBuffer.append(capitalize(line)).append("<br/>\n");
                continue;
            }

            // Dump remaining buffer content into output file
            if (pBuffer.length() > 0) {
                while (!bulletedListStack.isEmpty()) {
                    pBuffer.append("</ul>\n");
                    bulletedListStack.pop();
                }
                outputBuffer.append(wrapWitnP(pBuffer.toString()));
                pBuffer.setLength(0);   // To avoid miss this cleanup in the future
            }

            String templateContent = Files.readString(Path.of(templateFileName));
            return templateContent.replace(PLACEHOLDER_STRING, outputBuffer.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new IllegalStateException(ioe);
        }
    }

    public void generate(String inputFileName, String templateFileName, String htmlOutputFileName) throws IOException {
        String content = generate(inputFileName, templateFileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(htmlOutputFileName, StandardCharsets.UTF_8));) {
            writer.write(content);
        }
    }

    private void processPreBlock(BufferedReader reader, StringBuilder pBuffer) throws IOException {
        String line;
        StringBuilder preBuffer = new StringBuilder();
        boolean preStarted = true;
        while ((line = reader.readLine()) != null) {
            // End of PRE block
            if (line.startsWith("```")) {
                pBuffer.append(wrapWithPre(preBuffer.toString()));
                preStarted = false;
                break;
            }

            preBuffer.append(line).append("\n");
        }

        // After end of previous while <pre> block should be ended
        if (preStarted) {
            throw new IllegalStateException("Ending '```' was not found!");
        }
    }

    private String wrapWithLi(String str) {
        return String.format("<li>%s</li>\n", capitalize(str));
    }

    private String wrapWithPre(String buffer) {
        buffer = buffer
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
        return String.format("<pre>\n%s</pre>\n", buffer);
    }

    private String wrapWithB(String line) {
        return String.format("<b>%s</b><br/>\n", capitalize(line));
    }

    private String wrapWitnP(String buffer) {
        return String.format("<p align=\"justify\">\n%s</p>\n", buffer);
    }

    private String capitalize(String str) {
        if (str.startsWith("http://") || str.startsWith("https://")) {
            return String.format("<a href=\"%s\">%s</a>", str, str);
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            throw new IllegalArgumentException("Wrong amount of incoming params: inputFileName, templateFileName and htmlOutputFileName should be populated!");
        }

        String inputFileName = args[0];
        String templateFileName = args[1];
        String htmlOutputFileName = args[2];

        GithubPagesContentGenerator generator = new GithubPagesContentGenerator();
        generator.generate(inputFileName, templateFileName, htmlOutputFileName);
    }
}
