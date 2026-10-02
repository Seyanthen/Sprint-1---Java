# ClassPass Gradebook

ClassPass is a JavaFX student gradebook for managing students, assignments, groups, and grades. Gradebook data can be saved to and loaded from JSON files.

AI DISCLAIMER: The code in this project was made with Codex Agentic Programming. Codex was given engineered prompts to conform to project requirements. AI was used in the planning, development, and testing of this project.

## Development Environment

- Windows 10 or later
- JDK 25
- Visual Studio Code
- The [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)

Maven is provided by the Maven Wrapper, and JavaFX and the other project dependencies are downloaded automatically by Maven. An internet connection is needed the first time you build or run the project.

## Instructions for Build and Use

1. Install JDK 25 and the Extension Pack for Java.
2. Clone this repository:

   ```powershell
   git clone https://github.com/Seyanthen/Sprint-1-Java-Gradebook.git
   cd Sprint-1-Java-Gradebook
   ```

3. In VS Code, open the cloned repository folder with **File > Open Folder**. If prompted, select your JDK 25 installation and allow the Java extensions to import the Maven project.
4. Open the VS Code terminal and run the app:

   ```powershell
   cd gradebook-system
   .\mvnw.cmd clean javafx:run
   ```

The first run downloads Maven (version 3.9.16), JavaFX, and the other dependencies. No separate Maven or JavaFX installation is needed.

## Build and test

Run these commands from `gradebook-system` in the VS Code terminal:

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd test
```

To run the app without cleaning or packaging first:

```powershell
.\mvnw.cmd javafx:run
```

You can also run **Launch Gradebook App** from VS Code's Run and Debug panel after the Java extension has imported the project.

## Project layout

```text
gradebook-system/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .mvn/wrapper/
└── src/
    ├── main/java/com/gradebook/
    └── test/java/com/gradebook/
```

The Maven project targets Java 25 and uses JavaFX 25.0.2. Build output is generated under `gradebook-system/target/` and is not required in the repository.

## Troubleshooting

- Check `java -version` in the VS Code terminal; it should report Java 25. If not, set `JAVA_HOME` to your JDK 25 installation and restart VS Code.
- If Maven cannot download dependencies, check your internet connection and try the Maven Wrapper command again.
- If VS Code has not recognized the project, run **Java: Clean Java Language Server Workspace** from the Command Palette, then allow the project to reload.

## Useful Websites to Learn More

I found these websites useful in developing this software:

* [W3 Schools Java Tutorial](https://www.w3schools.com/java/default.asp)
* [O'Reilly Online Course](https://learning.oreilly.com/course/learn-java-from/9781838556976/)
* [O'Reilly Book](https://learning.oreilly.com/library/view/core-java-vol/9780135558553/)
* [Youtube Java Tutorial](https://www.youtube.com/watch?v=RRubcjpTkks)
* [Youtube Java Basics Playlist](https://www.youtube.com/watch?v=23HFxAPyJ9U&list=PLZPZq0r_RZOOj_NOZYq_R2PECIMglLemc)

## Future Work
The following items I plan to fix, improve, and/or add to this project in the future:

* [ ] Implement Grading by Groups
* [ ] Update UI for a cleaner experience
