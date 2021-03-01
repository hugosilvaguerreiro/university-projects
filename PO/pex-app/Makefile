JAVADIR=/usr/share/java
JARNAME=pex-app

all:
	(cd src; javac -cp $(JAVADIR)/po-uuilib.jar:../../pex-core/pex-core.jar -encoding UTF-8 `find . -name \*.java`)
	(cd src; jar cf ../$(JARNAME).jar `find . -name \*.class -o -name \*.java` )

clean:
	$(RM) $(JARNAME).jar `find . -name \*.class`
