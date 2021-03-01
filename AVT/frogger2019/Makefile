CC   = g++
LD   = g++
CFLAGS = -I./engine/lib -I./engine/geometry -I./engine/headers -I./engine/models -I./game/headers -g
LDFLAGS= -lGLEW -lGL -lGLU -lglut

SRC_DIR := .
OBJ_DIR := ./obj
SRC_FILES := $(shell find . -type f -name "*.cpp")
OBJ_FILES := $(patsubst $(SRC_DIR)/%.cpp,$(OBJ_DIR)/%.o,$(SRC_FILES))
OBJ_DIRS := $(dir $(OBJ_FILES))

# A phony target is one that is not really the name of a file
# https://www.gnu.org/software/make/manual/html_node/Phony-Targets.html
.PHONY: all clean run

all: frogger

frogger: $(OBJ_FILES)
	$(LD) $(CFLAGS) -o frogger $(OBJ_FILES) $(LDFLAGS)


DUMMY:=$(shell mkdir --parents $(OBJ_DIRS))
$(OBJ_DIR)/%.o: $(SRC_DIR)/%.cpp
	$(LD) $(CFLAGS) -c -o $@ $<

clean:
	@echo Cleaning...
	rm -rf obj frogger

run: frogger
	./frogger
