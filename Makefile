JAVAC = javac
JAVA = java

BIN				= bin/

SOURCES 		= \
				  src/java.Main.java \
				  src/java.Client.java \
				  src/java.conn/TCPClientHandler.java \
				  src/java.conn/UDPServer.java \
				  src/java.conn/TCPServer.java \
				  src/java.data/Tags.java \
				  src/java.data/Parser.java \
				  src/java.db/MyDB.java \
				  
LIBRARIES		= lib/java-getopt-1.0.14.jar:lib/sqlite-jdbc-3.8.11.2.jar
				  
default:
	if [ ! -d "bin" ]; then mkdir bin; fi
	$(JAVAC) -cp $(LIBRARIES) -d $(BIN) $(SOURCES)
	 
clean:
	rm -frv $(BIN)
	rm np.sqlite
	
rebuild: clean default
	
run:
	cd $(BIN)
	$(JAVA) -cp $(LIBRARIES) java.Main
	cd ..