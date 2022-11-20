COMPCD=cd src
COMP=javac
COMPARGS=-J-Dfile.encoding=UTF-8 -d ../bin ide/main/Start.java

RUNCD=cd bin
RUN=java
RUNARGS=-Dfile.encoding=UTF-8 ide.main.Start -cp .

MANIFESTNAME=../manifest.txt
MANIFESTCONTENT=Main-Class: ide.main.Start

JARCD=cd bin
JAR=jar
JARARGS=cfm ../Boot-IDE.jar $(MANIFESTNAME) *
LAUNCHJAR=java -jar Boot-IDE.jar

EXECOM=launch4jc
EXEARGS=exe-template.xml

# ---

build: compile
all: compile
jar: manifest createjar clean

compile:
	$(COMPCD) && \
	$(COMP) $(COMPARGS)

run:
	$(RUNCD) && \
	$(RUN) $(RUNARGS)

createjar:
	$(JARCD) && \
	$(JAR) $(JARARGS)

launchjar:
	$(LAUNCHJAR)

manifest:
	$(JARCD) && \
	echo $(MANIFESTCONTENT) >> $(MANIFESTNAME)

clean:
	$(JARCD) && \
	del $(MANIFESTNAME)

exe:
	$(EXECOM) $(EXEARGS)