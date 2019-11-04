package dev.kentrichards;

import name.fraser.neil.plaintext.diff_match_patch;

import java.awt.Desktop;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class DictationDiff {

    public static void main(String[] args) {
        if (args.length != 2 && args.length != 3) {
            System.out.println("Invalid arguments. Expected format:\n\t" +
                    "DictationDiff <transcript.txt> <dictations.txt> <version (optional)>\n" +
                    "Closing..");
            System.exit(1);
        }

        try {
            ArrayList<String> original = readFile(args[0]), dictations = readFile(args[1]);

            String version;
            if (args.length == 3) {
                version = args[2];
            } else {
                version = "Not specified.";
            }

            createVisualDiff(original.get(0), dictations, version);

        } catch (FileNotFoundException e) {
            System.out.println("File not found. Input files should be located in \"/dictations/input/\"\nClosing..");
            System.exit(2);
        }
    }

    /**
     * Outputs a visual diff between the contents of original and each index of ArrayList dictations. Uses Google's
     * diff-match-patch library to calculate the diff. Outputs as HTML file, that is opened automatically.
     *
     * @param original   String containing human transcript of some audio file
     * @param dictations Talkatoo generated dictations of the same audio file
     * @param version    optional Talkatoo version, given as a command line argument
     */
    private static void createVisualDiff(String original, ArrayList<String> dictations, String version) {
        diff_match_patch dmp = new diff_match_patch();
        String timeStamp = getTime();
        String fileName = createFileName(timeStamp, version);

        int totalWords = countWords(original);

        LinkedList<diff_match_patch.Diff> diff;
        try (PrintWriter writer = new PrintWriter("dictations/output/" + fileName, StandardCharsets.UTF_8)) {
            // Output current time, Talkatoo version, accuracy formula and the original text at the top of the file
            writer.println("<b>" + timeStamp + "</b><br>Talkatoo Version: " + version +
                    "<br>Accuracy = (total words - errors) / (total words)<br><br>");
            writer.println("<b>ORIGINAL FILE (" + totalWords + " Words):</b><br>" + original + "<br><br>");

            int index = 1, numInserts = 0, numDeletes = 0;
            double accuracy;
            for (String dictation : dictations) {
                diff = dmp.diff_main(original, dictation);

                // Get human readable diff
                dmp.diff_cleanupSemantic(diff);

                // Tally number of differences between current dictation and original
                for (diff_match_patch.Diff item : diff) {
                    switch (item.operation) {
                        case INSERT:
                            numInserts++;
                            break;
                        case DELETE:
                            numDeletes++;
                            break;
                    }
                }

                // Calculate diff accuracy to two decimal places
                accuracy = ((double) totalWords - (numInserts + numDeletes)) / (double) totalWords * 100.0;
                accuracy = Math.round(accuracy * 100.0) / 100.0;

                // Output visual diff
                writer.println("<b>DICTATION " + index + " DIFF (Insertions: " + numInserts +
                        ", Deletions: " + numDeletes + ", Accuracy: " + accuracy + "%):</b><br>");
                writer.println(dmp.diff_prettyHtml(diff) + "<br><br>");

                numInserts = numDeletes = 0;
                index++;
            }

            // Open HTML file containing visual diffs
            File htmlFile = new File("dictations/output/" + fileName);
            Desktop.getDesktop().browse(htmlFile.toURI());

        } catch (IOException e) {
            System.out.println("Unable to create output file. Ensure \"/dictations/output\" folder exists.\nClosing..");
            System.exit(3);
        }
    }

    /**
     * Reads a text file line by line, storing each nonempty line as a String in an ArrayList that is returned.
     *
     * @param file text file containing either a single human transcription of an audio file,
     *             or a text file containing one or many Talkatoo dictations of the same audio file
     * @return ArrayList containing the files contents, each line of the file is stored in it's own array index
     * @throws FileNotFoundException thrown if the given file does not exist in '/dictations/input/' or that directory does not exist
     */
    private static ArrayList<String> readFile(String file) throws FileNotFoundException {
        // Automatically closes Scanner
        try (Scanner in = new Scanner(new FileReader("dictations/input/" + file))) {
            ArrayList<String> out = new ArrayList<>();
            String temp;

            while (in.hasNextLine()) {
                temp = in.nextLine();

                // Only add lines that aren't empty
                if (temp.length() > 0) {
                    // Not worried about inconsistent capitalization or trailing spaces
                    out.add(temp.toLowerCase().trim());
                }
            }

            return out;
        }
    }

    /**
     * Creates a unique file name for the output of a visual diff.
     *
     * @param timeStamp current local time in YYYY/MM/DD HH:MM:SS format
     * @param version   current app version if specified, "Not specified." otherwise
     * @return unique file name with format "version timeStamp.html" or just "timeStamp.html"
     */
    private static String createFileName(String timeStamp, String version) {
        String fileName = timeStamp.replaceAll("[/ :]", ".") + ".html";

        if (!version.equals("Not specified.")) {
            fileName = version + " " + fileName;
        }

        return fileName;
    }

    /**
     * Returns the current local time as as formatted String.
     *
     * @return current time in Halifax, Nova Scotia
     */
    private static String getTime() {
        return ZonedDateTime.now(
                ZoneId.of("Canada/Atlantic"))
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
    }

    /**
     * Counts the number of words in a String.
     *
     * @param text String containing a dictation or human transcription of an audio file
     * @return number of words in the given String
     */
    private static int countWords(String text) {
        if (text.isEmpty()) {
            return 0;
        }

        // Splits on any whitespace
        return text.split("\\s+").length;
    }
}
