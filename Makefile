COMPCD=cd src
COMP=javac
COMPARGS=-J-Dfile.encoding=UTF-8 -d ../bin ide/main/Start.java

RUNCD=cd bin
RUN=java
RUNARGS=-Dfile.encoding=UTF-8 ide.main.Start -cp .

MANIFESTNAMEROOT=manifest.txt
MANIFESTNAME=../manifest.txt
MANIFESTCONTENT=Main-Class: ide.main.Start

JARNAME=Boot-IDE.jar
JARCD=cd bin
JAR=jar
JARARGS=cfm ../$(JARNAME) $(MANIFESTNAME) *
LAUNCHJAR=java -jar $(JARNAME)

EXECOM=launch4jc
EXEARGS=exe-template.xml

LINNAME=Boot-IDE

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
	echo $(MANIFESTCONTENT) > $(MANIFESTNAME)

clean:
	del $(MANIFESTNAMEROOT)

cleanlin:
	rm $(MANIFESTNAMEROOT)

exe:
	$(EXECOM) $(EXEARGS)

lin:
	echo '#!/usr/bin/java -jar' > $(LINNAME)
	cat $(JARNAME) >> $(LINNAME)
	chmod +x $(LINNAME)