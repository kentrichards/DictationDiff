package dev.kentrichards;

import name.fraser.neil.plaintext.diff_match_patch;

import java.io.FileNotFoundException;
import java.io.FileReader;
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
                       revision = readFile(args[1], true).toString();

                createVisualDiff(original, revision);
                break;
            default:
                // error
                System.exit(1);
        }
    }

    private static void createVisualDiff(String original, String revision) {

    }

    private static Object readFile(String file, boolean multiline) {
        // Automatically closes Scanner
        try (Scanner in = new Scanner(new FileReader("dictations/" + file))) {

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
