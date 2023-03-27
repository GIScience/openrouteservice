---
parent: Installation and Usage
nav_exclude: true
title: Opening openrouteservice code in IntelliJ
---

# Opening openrouteservice code in IntelliJ

Though IntelliJ provides functionality to directly clone a repository, the steps below are the general procedure for getting openrouteservice up and running in IntelliJ:
1. Clone the repository into a folder
2. Open IntelliJ and Create a project from existing source (File -> New -> Project from Existing Source)
3. Select the folder that was just cloned (it should contain the subfolders of docker and openrouteservice) and click OK
4. Select the option for "Create Project from Existing Source"
5. When you reach a page asking to "choose directories that will be added as project roots", deselect all items and then click Next
6. When the project has been loaded, you should only see the files that are contained in the root openrouteservice folder and no subdirectories.
7. Click File -> Project Structure
8. In the Project tab, select 17 as the Java SDK
9. Go to the Modules tab and click the "+" button and then select "Import Module"
10. Select the openrouteservice folder (not the root one, but the one containing the src folder) and click Next
11. Select to "Import module from external model" and choose Maven.
12. Click through to Finish and the module will be added.
13. Repeat this for the Docker module, but select "Create module from existing sources" rather than "Import module from external model"
14. Create a maven run configuration with the following `Run` settings: `spring-boot:run -Dspring-boot.run.fork=false`. This allows for local debugging.
