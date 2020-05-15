BIN_FILES  = server Client.class User.class upper/UpperPublisher.class upper/UpperService.class

CC = gcc


CCGLAGS =	-Wall  -g

LDFLAGS = -L$(INSTALL_PATH)/lib/
LDLIBS = -lpthread -lsqlite3


all: CFLAGS=$(CCGLAGS)
all: $(BIN_FILES)
.PHONY : all

server: server.o lines.o
	$(CC) $(LDFLAGS) $^ $(LDLIBS) -o $@

%.o: %.c
	$(CC) $(CPPFLAGS) $(CFLAGS) -c $<

upper/UpperService.class: upper/UpperService.java
	javac -cp jaxws-ri/lib/*:. upper/UpperService.java

upper/UpperPublisher.class: upper/UpperPublisher.java upper/UpperService.class
	javac -cp jaxws-ri/lib/*:. upper/UpperPublisher.java

Client.class User.class: Client.java User.java upper/UpperPublisher.class
	javac *.java

clean:
	rm -f $(BIN_FILES) *.o *.db

.SUFFIXES:
.PHONY : clean
