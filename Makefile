COMPCD=cd src
COMP=javac
COMPARGS=-d ../bin ide/main/Start.java

RUNCD=cd bin
RUN=java
RUNARGS=ide.main.Start -cp .

build: compile

compile:
	$(COMPCD) && \
	$(COMP) $(COMPARGS)

run:
	$(RUNCD) && \
	$(RUN) $(RUNARGS)
