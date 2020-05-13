BIN_FILES  = server Client.class User.class UpperPublisher.class UpperService.class

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

Client.class User.class UpperPublisher.class UpperService.class: Client.java User.java UpperPublisher.java UpperService.java
	javac *.java

clean:
	rm -f $(BIN_FILES) *.o *.db

.SUFFIXES:
.PHONY : clean
