package by.andd3dfx;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Usage example:
 * java -jar github-pages-content-generator.jar inputFileName templateFileName htmlOutputFileName
 */
public class GithubPagesContentGenerator {

    private static final String TITLE_PLACEHOLDER = "***TITLE_PLACEHOLDER***";
    private static final String PLACEHOLDER_STRING = "***CONTENT_PLACEHOLDER***";

    public String generate(String inputFileName, String templateFileName) throws IOException {
        String title = "";
        StringBuilder outputBuffer = new StringBuilder();
        try (
                BufferedReader inputFileReader = new BufferedReader(new FileReader(inputFileName, StandardCharsets.UTF_8));
        ) {
            String line;
            StringBuilder pBuffer = new StringBuilder();

            while ((line = inputFileReader.readLine()) != null) {

                if (line.isBlank()) {
                    dumpBufferIntoOutputFile(pBuffer, outputBuffer);
                    continue;
                }

                if (line.startsWith("! ")) {
                    title = line.substring(2);
                    continue;
                }

                if (line.startsWith("** ")) {
                    processH1Block(line, pBuffer, outputBuffer);
                    continue;
                }

                if (line.startsWith("*** ")) {
                    processH2Block(line, pBuffer, outputBuffer);
                    continue;
                }

                // Title of new block
                if (line.startsWith("* ") || line.startsWith("- ")) {
                    processTitleBlock(line, pBuffer, outputBuffer);
                    continue;
                }

                // Start of PRE block
                if (line.startsWith("```")) {
                    processPreBlock(inputFileReader, pBuffer);
                    continue;
                }

                processUsualLine(line, pBuffer);
                continue;
            }

            dumpBufferIntoOutputFile(pBuffer, outputBuffer);

            String templateContent = Files.readString(Path.of(templateFileName));
            return templateContent
                    .replace(TITLE_PLACEHOLDER, title)
                    .replace(PLACEHOLDER_STRING, outputBuffer.toString());
        }
    }

    private void processH1Block(String line, StringBuilder pBuffer, StringBuilder outputBuffer) {
        outputBuffer.append(wrapWitH1(line.substring(3)));
    }

    private void processH2Block(String line, StringBuilder pBuffer, StringBuilder outputBuffer) {
        outputBuffer.append(wrapWitH2(line.substring(4)));
    }

    private void processUsualLine(String line, StringBuilder pBuffer) {
        // Remove starting/ending spaces
        // line = line.trim();

        // Usual line: just write it capitalized
        pBuffer.append(capitalize(line)).append("<br/>\n");
    }

    private void processTitleBlock(String line, StringBuilder pBuffer, StringBuilder outputBuffer) {
        dumpBufferIntoOutputFile(pBuffer, outputBuffer);

        // Title line
        pBuffer.append(wrapWithB(line.substring(2)));
    }

    private void dumpBufferIntoOutputFile(StringBuilder pBuffer, StringBuilder outputBuffer) {
        if (pBuffer.length() > 0) {
            outputBuffer.append(wrapWitnP(pBuffer.toString()));
            pBuffer.setLength(0);
        }
    }

    public void generate(String inputFileName, String templateFileName, String htmlOutputFileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(htmlOutputFileName, StandardCharsets.UTF_8));) {
            String content = generate(inputFileName, templateFileName);
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

    private String wrapWitH1(String substring) {
        return String.format("<h1>%s</h1><hr/>\n", substring);
    }

    private String wrapWitH2(String substring) {
        return String.format("<h2>%s</h2><hr/>\n", substring);
    }

    private String wrapWithPre(String str) {
        str = str
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
        return String.format("<pre>\n%s</pre>\n", str);
    }

    private String wrapWithB(String line) {
        return String.format("<b>%s</b><br/>\n", capitalize(line));
    }

    private String wrapWitnP(String str) {
        return String.format("<p align=\"justify\">\n%s</p>\n", str);
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
