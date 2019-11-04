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
                    "DictationDiff <originalFile.txt> <dictationsFile.txt> <talkatooVersion> (optional)\n" +
                    "Closing..");
            System.exit(1);
        }

        try {
            ArrayList<String> original = readFile(args[0]),
                              dictations = readFile(args[1]);

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

    private static void createVisualDiff(String original, ArrayList<String> dictations, String version) {
        diff_match_patch dmp = new diff_match_patch();
        String timeStamp = getTime();
        String fileName = createFileName(timeStamp, version);

        LinkedList<diff_match_patch.Diff> diff;
        try (PrintWriter writer = new PrintWriter("dictations/output/" + fileName, StandardCharsets.UTF_8)) {
            writer.println("<b>" + timeStamp + "</b><br>Talkatoo Version: " + version + "<br><br>");
            writer.println("<b>ORIGINAL FILE:</b><br>" + original + "<br><br>");

            int index = 1, numInserts = 0, numDeletes = 0;
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

                // Output visual diff
                writer.println("<b>DICTATION " + index + " DIFF (Insertions: " + numInserts + ", Deletions: " + numDeletes + "):</b><br>");
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

    private static ArrayList<String> readFile(String file) throws FileNotFoundException {
        // Automatically closes Scanner
        try (Scanner in = new Scanner(new FileReader("dictations/input/" + file))) {
            ArrayList<String> out = new ArrayList<>();
            String temp;

            while (in.hasNextLine()) {
                temp = in.nextLine();

                // Only add lines that aren't empty
                if (temp.length() > 0) {
                    out.add(temp.toLowerCase());
                }
            }

            return out;
        }
    }

    private static String createFileName(String timeStamp, String version) {
        String fileName = timeStamp.replaceAll("[/ :]", ".") + ".html";

        if (!version.equals("Not specified.")) {
            fileName = version + " " + fileName;
        }

        return fileName;
    }

    private static String getTime() {
        return ZonedDateTime.now(
                ZoneId.of("Canada/Atlantic"))
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
    }
}
