**How to Use (Requires JRE)**<br>
- Build Jar file or download it from "out/artifacts/DictationDiff_jar"
- In the directory the Jar file is located, create a folder called "dictations"
- Inside "dictations", create two folders, "input" and "output"
- Place the files you want to compare in "input" (e.g., the human transcript of an audio file, and the dictations of that same audio file).
- Open the Windows Command Prompt and navigate to the folder containing the Jar and the folders you just created.
- In CMD type in "java -jar DictationDiff.jar <transcript.txt> <dictations.txt> <version (optional)>"
- If no errors occur, the HTML file containing the visual diff will automatically open. If an error occurs it will be displayed in the Command Prompt.