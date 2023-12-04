JFLAGS = -Xlint:unchecked

CLASSPATH= lib/*:.


JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) -cp $(CLASSPATH) $*.java

# This uses the line continuation character (\) for readability
# You can list these all on a single line, separated by a space instead.
# If your version of make can't handle the leading tabs on each
# line, just remove them (these are also just added for readability).
CLASSES = \
	anthill/util.java \
	anthill/Drone.java \
	anthill/PageDisplay.java \
	LogServer.java \
	LogBot.java \


default: classes

PageDisplay.class: anthill/PageDisplay.java
	javac $(JFLAGS) -cp $(CLASSPATH) anthill/PageDisplay.java
util.class: anthill/util.java
	javac $(JFLAGS) -cp $(CLASSPATH) anthill/util.java
Drone.class: anthill/Drone.java
	javac $(JFLAGS) -cp $(CLASSPATH) anthill/Drone.java
LogServer.class: LogServer.java
	javac $(JFLAGS) -cp $(CLASSPATH) LogServer.java
LogBot.class: LogBot.java
	javac $(JFLAGS) -cp $(CLASSPATH) LogBot.java

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

