JAVAC = javac
JAVA = java

BIN				= bin/

SOURCES 		= \
				  src/main/Server.java \
				  src/main/Client.java \
				  src/main/conn/TCPClientHandler.java \
				  src/main/conn/UDPServer.java \
				  src/main/conn/TCPServer.java \
				  src/main/data/Tags.java \
				  src/main/data/Parser.java \
				  src/main/data/Events.java \
				  src/main/data/Project.java \
				  src/main/data/Task.java \
				  src/main/utils/CalHelper.java \
				  src/main/db/MyDB.java \
				  src/main/ASN1/ASN1_Util.java \
				  src/main/ASN1/ASN1DecoderFail.java \
				  src/main/ASN1/ASNLenRuntimeException.java \
				  src/main/ASN1/ASNObj.java \
				  src/main/ASN1/ASNObjArrayable.java \
				  src/main/ASN1/Decoder.java \
				  src/main/ASN1/Encoder.java \
				  
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
	$(JAVA) -cp $(LIBRARIES) Server
	cd ..