package dev.kentrichards;

import name.fraser.neil.plaintext.diff_match_patch;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class DictationDiff {

    public static void main(String[] args) {
        switch (args.length) {
            case 2:
                // call function
                System.out.println("Two args");
                break;
            case 3:
                String original = readFile(args[0], false).toString(),
                       revision = readFile(args[1], false).toString();

                createVisualDiff(original, revision);
                break;
            default:
                // error
                System.exit(1);
        }
    }

    private static void createVisualDiff(String original, String revision) {
        diff_match_patch dmp = new diff_match_patch();
        LinkedList<diff_match_patch.Diff> diff = dmp.diff_main(original, revision);
        dmp.diff_cleanupSemantic(diff);

        String timeStamp = ZonedDateTime.now(ZoneId.of("Canada/Atlantic")).format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss"));

        try (PrintWriter writer = new PrintWriter("dictations/output/" + timeStamp + ".html", StandardCharsets.UTF_8)) {
            writer.println(dmp.diff_prettyHtml(diff));
        } catch (IOException e) {
            // error
            System.exit(1);
        }
    }

    private static Object readFile(String file, boolean multiline) {
        // Automatically closes Scanner
        try (Scanner in = new Scanner(new FileReader("dictations/input/" + file))) {

            if (multiline) {
                ArrayList<String> out = new ArrayList<>();
                while (in.hasNextLine()) {
                    out.add(in.nextLine());
                }
                return out;
            } else {
                return in.nextLine();
            }
        } catch (FileNotFoundException e) {
            // error
            System.exit(1);
            return 1;
        }
    }
}
