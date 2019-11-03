package dev.kentrichards;

import name.fraser.neil.plaintext.diff_match_patch;

import java.awt.*;
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
            System.out.println("Invalid arguments. Closing..");
            System.exit(1);
        }

        try {
            ArrayList<String> original = readFile(args[0]),
                              dictations = readFile(args[1]);

            String version;
            if (args.length == 3) {
                version = args[2];
            } else {
                version = "No version specified.";
            }

            createVisualDiff(original.get(0), dictations, version);
        } catch (FileNotFoundException e) {
            System.out.println("File not found. Closing..");
            System.exit(1);
        }
    }

    private static void createVisualDiff(String original, ArrayList<String> revisions, String version) {
        diff_match_patch dmp = new diff_match_patch();
        String timeStamp = ZonedDateTime.now(ZoneId.of("Canada/Atlantic")).format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        String fileName = timeStamp.replaceAll("[/ :]", ".") + ".html";

        LinkedList<diff_match_patch.Diff> diff;
        try (PrintWriter writer = new PrintWriter("dictations/output/" + fileName, StandardCharsets.UTF_8)) {
            writer.println("<b>" + timeStamp + "</b><br>Talkatoo Version: " + version + "<br><br>");
            writer.println("<b>ORIGINAL FILE:</b><br>" + original + "<br><br>");

            int index = 1, numInserts = 0, numDeletes = 0;
            for (String revision : revisions) {
                diff = dmp.diff_main(original, revision);
                dmp.diff_cleanupSemantic(diff);

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

                writer.println("<b>REVISION " + index + " DIFF (Insertions: " + numInserts + ", Deletions: " + numDeletes + "):</b><br>");
                writer.println(dmp.diff_prettyHtml(diff) + "<br><br>");
                numInserts = 0;
                numDeletes = 0;
                index++;
            }

            File htmlFile = new File("dictations/output/" + fileName);
            Desktop.getDesktop().browse(htmlFile.toURI());
        } catch (IOException e) {
            // error
            System.exit(1);
        }
    }

    private static ArrayList<String> readFile(String file) throws FileNotFoundException{
        // Automatically closes Scanner
        try (Scanner in = new Scanner(new FileReader("dictations/input/" + file))) {
            ArrayList<String> out = new ArrayList<>();
            String temp;
            while (in.hasNextLine()) {
                temp = in.nextLine();
                if (temp.length() > 0) {
                    out.add(temp);
                }
            }
            return out;
        }
    }
}
