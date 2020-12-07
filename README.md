# Timer
Task timer in JavaFX and HSQLDB

A personal app that tracks the time spend on a task. There are Activities and Tasks. Activities are uniquely named. Every tasks belong to one Activity only and are not uniquely named.

The assumption is that you will only be working on one task at the time. Therefore when the application starts it closes remaining tasks from previous day's and searches the last opened task from to day. If that is found it will be presented and can only be closed.



If you have HSQLDB on c:\hsqldb-2.5.1 JavaFX on c:\javafx-sdk-15.0.1 then from the root of the app path the following commands can be used to compile and run the application.

compile:
javac -d dist -classpath c:\hsqldb-2.5.1\hsqldb\lib\\* --module-path c:\javafx-sdk-15.0.1\lib --add-modules javafx.controls standard\\*.java

run:
java -classpath .\dist;c:\hsqldb-2.5.1\hsqldb\lib\\* --module-path c:\javafx-sdk-15.0.1\lib --add-modules javafx.controls standard.HelloFX
