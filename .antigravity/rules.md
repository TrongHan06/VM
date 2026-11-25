\# Project Rules for Gradle \& Terminal Execution (Windows)



When running any Gradle command (`.\\gradlew.bat` or `gradlew`) in this project, you MUST strictly follow these rules to prevent terminal buffer hangs and "Empty log" issues:



1\. \*\*NEVER use `--no-daemon`\*\*: Do not pass `--no-daemon`. Always let Gradle reuse the existing daemon configured with `org.gradle.java.home` in `gradle.properties`.

2\. \*\*Redirect output to a log file\*\*: Windows agent terminals often buffer stdout and return "Empty log" during long Gradle builds. Always redirect output to `gradle\_build.log` and read the file afterward to inspect the result.

&#x20;  - Example command: `.\\gradlew.bat testDebugUnitTest --offline --console=plain > gradle\_build.log 2>\&1`

&#x20;  - Immediately after the command finishes, read `gradle\_build.log` (especially the end of the file or lines containing `FAILURE:` / `error:`) to verify the build status.

3\. \*\*Avoid interactive prompts\*\*: Always append `--console=plain` and avoid any interactive flags.

4\. \*\*If a Gradle command hangs\*\*: Run `.\\gradlew.bat --stop` before retrying.



