JFLAGS = -g -Xlint:unchecked

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
	anthill/Drone.java \
	anthill/PageDisplay.java \

default: classes

Drone.class: anthill.Drone.java
	javac $(JFLAGS) -cp $(CLASSPATH) Drone.java
anthill.PageDisplay.class: anthill.PageDisplay.java
	javac $(JFLAGS) -cp $(CLASSPATH) anthill.PageDisplay.java
classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

