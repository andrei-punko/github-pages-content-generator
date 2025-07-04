package by.andd3dfx;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Usage example:
 * java -jar github-pages-content-generator.jar inputFileName templateFileName htmlOutputFileName
 */
public class GithubPagesContentGenerator {

    private static final String TITLE_PLACEHOLDER = "***TITLE_PLACEHOLDER***";
    private static final String PLACEHOLDER_STRING = "***CONTENT_PLACEHOLDER***";
    private Map<String, Integer> headerToAmountMap = new HashMap<>();
    private String currentTags = "EMPTY";

    public String generate(String inputFileName, String templateFileName) throws IOException {
        String title = "";
        StringBuilder outputBuffer = new StringBuilder();
        try (
                var inputFileReader = new BufferedReader(new FileReader(inputFileName, UTF_8));
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

                // Start of CODE block
                if (line.startsWith("```")) {
                    processCodeBlock(inputFileReader, pBuffer);
                    continue;
                }

                if (line.startsWith("IMG")) {
                    processImageBlock(line, pBuffer);
                    continue;
                }

                if (line.startsWith("[")) {
                    currentTags = line.substring(1, line.length() - 1);
                    continue;
                }

                processUsualLine(line, pBuffer);
            }

            dumpBufferIntoOutputFile(pBuffer, outputBuffer);
            System.out.printf("Links generated: %d%n", linkCounter);

            String templateContent = Files.readString(Path.of(templateFileName));
            return templateContent
                    .replace(TITLE_PLACEHOLDER, title)
                    .replace(PLACEHOLDER_STRING, outputBuffer.toString());
        }
    }

    private void processImageBlock(String line, StringBuilder pBuffer) {
        pBuffer.append(wrapWithImg(line.replaceFirst("IMG\\s+", "")));
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
        line = line
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
        pBuffer.append(capitalize(line)).append("<br/>\n");
    }

    private void processTitleBlock(String line, StringBuilder pBuffer, StringBuilder outputBuffer) {
        dumpBufferIntoOutputFile(pBuffer, outputBuffer);

        // Title line
        pBuffer.append(wrapWithB(escapeAngleBrackets(line.substring(2))));
    }

    private void dumpBufferIntoOutputFile(StringBuilder pBuffer, StringBuilder outputBuffer) {
        if (!pBuffer.isEmpty()) {
            outputBuffer.append(wrapWithP(pBuffer.toString()));
            pBuffer.setLength(0);
        }
    }

    public String generate(String inputFileName, String templateFileName, String htmlOutputFileName) throws IOException {
        try (var writer = new BufferedWriter(new FileWriter(htmlOutputFileName, UTF_8));) {
            String content = generate(inputFileName, templateFileName);
            writer.write(content);
            return content;
        }
    }

    private void processCodeBlock(BufferedReader reader, StringBuilder pBuffer) throws IOException {
        String line;
        StringBuilder codeTagBuffer = new StringBuilder();
        boolean codeTagStarted = true;
        while ((line = reader.readLine()) != null) {
            // End of CODE block
            if (line.startsWith("```")) {
                pBuffer.append(wrapWithCode(codeTagBuffer.toString()));
                codeTagStarted = false;
                break;
            }

            codeTagBuffer.append(line).append("\n");
        }

        // After end of previous while <code> block should be ended
        if (codeTagStarted) {
            throw new IllegalStateException("Ending '```' was not found!");
        }
    }

    private String wrapWithImg(String substring) {
        return String.format("<img src=\"%s\"><br/>\n", substring);
    }

    private String wrapWitH1(String substring) {
        return String.format("<h1>%s</h1><hr/>\n", substring);
    }

    private String wrapWitH2(String substring) {
        return String.format("<h2>%s</h2><hr/>\n", substring);
    }

    private String escapeAngleBrackets(String str) {
        return str
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }

    private String wrapWithCode(String str) {
        return String.format("<code>%s</code>\n", escapeAngleBrackets(str));
    }

    private int linkCounter = 0;

    private String wrapWithB(String line) {
        linkCounter++;
        int linkId = determineLinkId(line);
        return String.format("<a href=\"#q%d\" id=\"q%d\" class=\"a-title\">%s</a><br/>\n",
                linkId, linkId, capitalize(line));
    }

    private int determineLinkId(String line) {
        if (headerToAmountMap.containsKey(line)) {
            headerToAmountMap.put(line, headerToAmountMap.get(line) + 1);
            return Objects.hash(line, headerToAmountMap.get(line));
        }

        headerToAmountMap.put(line, 1);
        return line.hashCode();
    }

    private String wrapWithP(String str) {
        return String.format("<p style=\"text-align:justify\" tags=\""+currentTags+"\">\n%s</p>\n", str);
    }

    @SneakyThrows
    private String capitalize(String str) {
        if (str.startsWith("http://") || str.startsWith("https://")) {
            var decodedStr = URLDecoder.decode(str, UTF_8);
            var decodedNEscapedStr = decodedStr.replace("+", "%2B");
            return String.format("<a href=\"%s\">%s</a>", decodedNEscapedStr, decodedStr);
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            throw new IllegalArgumentException("Wrong amount of incoming params: " +
                    "inputFileName, templateFileName and htmlOutputFileName should be populated!");
        }

        String inputFileName = args[0];
        String templateFileName = args[1];
        String htmlOutputFileName = args[2];

        new GithubPagesContentGenerator()
                .generate(inputFileName, templateFileName, htmlOutputFileName);
    }
}
