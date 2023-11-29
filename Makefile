JFLAGS = -g -Xlint:unchecked

CLASSPATH= lib/*:/usr/share/java/*:/usr/share/java/xmlrpc-client.jar:/usr/share/java/xmlrpc-server.jar:/usr/share/java/xmlrpc-common.jar:/usr/share/java/ws-commons-util.jar:/usr/share/java/commons-logging.jar:.


JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

# This uses the line continuation character (\) for readability
# You can list these all on a single line, separated by a space instead.
# If your version of make can't handle the leading tabs on each
# line, just remove them (these are also just added for readability).
CLASSES = \
	Drone.java \
	PageDisplay.java \

default: classes

Drone.class: Drone.java
	javac $(JFLAGS) -cp $(CLASSPATH) Drone.java
PageDisplay.class: PageDisplay.java
	javac $(JFLAGS) -cp $(CLASSPATH) PageDisplay.java
classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

