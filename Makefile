COMPCD=cd src
COMP=javac
COMPARGS=-d ../bin ide/main/Start.java

RUNCD=cd bin
RUN=java
RUNARGS=ide.main.Start -cp .

JARCD=cd bin
JAR=jar cf Boot-IDE.jar *
LAUNCHJAR=java -jar Boot-IDE.jar

build: compile
all: compile

compile:
	$(COMPCD) && \
	$(COMP) $(COMPARGS)

run:
	$(RUNCD) && \
	$(RUN) $(RUNARGS)

jar:
	$(JARCD) && \
	$(JAR)

launchjar:
	$(JARCD) && \
	$(LAUNCHJAR)
