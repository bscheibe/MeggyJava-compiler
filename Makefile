# Makefile for Javacup compiler.
# $ make - compiles all classes produces an MJ.jar in the testing directory.
# $ make three||four||five - runs MJ.jar on all PA3||PA4||PA5 test cases.
# $ make clean - removes all test files and .

JAVA_CUP_RUNTIME = $(shell pwd)

SRC_DIR = src
TEST_DIR = Testing
PARSE_DIR = $(SRC_DIR)/mjparser
AST_NODES_SRC = $(wildcard $(SRC_DIR)/ast/node/*)
AST_NODES_CLASS = $(AST_NODES_SRC:.java=.class)

THREE_SRC = $(wildcard $(TEST_DIR)/PA3/*.java)
THREE_S = $(THREE_SRC:.java=.s)
THREE_AST = $(wildcard $(TEST_DIR)/PA3/*.ast.dot)
THREE_PNG = $(THREE_AST:.ast.dot=.png)

FOUR_SRC = $(wildcard $(TEST_DIR)/PA4/*.java)
FOUR_A = $(FOUR_SRC:.java=.ast.dot)
FOUR_AST = $(wildcard $(TEST_DIR)/PA4/*.ast.dot)
FOUR_PNG = $(FOUR_AST:.ast.dot=.png)

FIVE_SRC = $(wildcard $(TEST_DIR)/PA5/*.java)
FIVE_A = $(FIVE_SRC:.java=.ast.dot)
FIVE_AST = $(wildcard $(TEST_DIR)/PA5/*.ast.dot)
FIVE_PNG = $(FIVE_AST:.ast.dot=.png)

PROG = MJ
JCC = javac
JAR = jar

.SUFFIXES: .java.class

all: $(PROG).jar

source: $(PARSE_DIR)/Yylex.java $(PARSE_DIR)/mj.java $(AST_NODES_CLASS)

$(TEST_DIR)/PA3/%.s: $(TEST_DIR)/PA3/%.java
	java -jar MJ.jar $^

$(TEST_DIR)/PA3/%.png: $(TEST_DIR)/PA3/%.ast.dot
	dot -Tpng -o $^.png $^


three: $(THREE_S) 

threepng: $(THREE_PNG)

$(TEST_DIR)/PA4/%.ast.dot: $(TEST_DIR)/PA4/%.java
	-java -jar MJ.jar $^

$(TEST_DIR)/PA4/%.png: $(TEST_DIR)/PA4/%.ast.dot
	dot -Tpng -o $^.png $^


four: $(FOUR_A) 

fourpng: $(FOUR_PNG)

$(TEST_DIR)/PA5/%.ast.dot: $(TEST_DIR)/PA5/%.java
	-java -jar MJ.jar $^

$(TEST_DIR)/PA5/%.png: $(TEST_DIR)/PA5/%.ast.dot
	dot -Tpng -o $^.png $^

five: $(FIVE_A) 

fivepng: $(FIVE_PNG)

.PHONY:clean
clean:
	rm -rf $(SRC_DIR)/*.class $(SRC_DIR)/*/*.class $(SRC_DIR)/*/*/*.class
	rm -f $(PARSE_DIR)/sym.java $(PARSE_DIR)/mj.java $(PARSE_DIR)/Yylex.java
	rm -rf META_INF
	rm -rf $(PROG).jar
	rm -rf $(SCANNER).jar
	rm -rf javacup.dump
	rm -rf $(TEST_DIR)/MJ.jar
	rm -rf $(TEST_DIR)/*/*.dot $(TEST_DIR)/*/*.png $(TEST_DIR)/*/*.s  $(TEST_DIR)/*/*.jar
	rm -rf $(TEST_DIR)/PA4/mjsimPictures ; rm -rf $(TEST_DIR)/PA4/output.log
	rm -rf $(TEST_DIR)/PA5/mjsimPictures ; rm -rf $(TEST_DIR)/PA5/output.log

### Final jar files.
$(PROG).jar: $(SRC_DIR)/$(PROG)Driver.class
	cd $(SRC_DIR); $(JAR) cmf $(PROG)MainClass.txt $(PROG).jar *.class */*.class */*/*.class -C $(JAVA_CUP_RUNTIME) java_cup  
	cd ..
	mv $(SRC_DIR)/$(PROG).jar .
	cp MJ.jar $(TEST_DIR)
	cp MJ.jar $(TEST_DIR)/PA3
	cp MJ.jar $(TEST_DIR)/PA4  
	cp MJ.jar $(TEST_DIR)/PA5  
	cp MJSIM.jar $(TEST_DIR)/PA4 ; cp reference_compiler.jar $(TEST_DIR)/PA4
	cp MJSIM.jar $(TEST_DIR)/PA5 ; cp reference_compiler.jar $(TEST_DIR)/PA5

#### mj parser
$(PARSE_DIR)/mj.java: $(PARSE_DIR)/mj.cup
	java -jar java-cup-11a.jar -expect 50 -parser  mj -dump $(PARSE_DIR)/mj.cup > javacup.dump 2>&1
	mv -f sym.java $(PARSE_DIR)/
	mv -f mj.java $(PARSE_DIR)/

#### lexer
$(PARSE_DIR)/Yylex.java: $(PARSE_DIR)/mj.lex $(PARSE_DIR)/mj.java
	java -jar JLex.jar $(PARSE_DIR)/mj.lex
	mv $(PARSE_DIR)/mj.lex.java $(PARSE_DIR)/Yylex.java

#### Compile the Main class.
$(SRC_DIR)/ast/node/%.class: $(SRC_DIR)/ast/node/%.java
	$(JCC) -classpath $(SRC_DIR) $^

MAINPROG_DEPS = $(SRC_DIR)/$(PROG)Driver.java  \
            $(PARSE_DIR)/Yylex.java $(PARSE_DIR)/mj.java

$(SRC_DIR)/$(PROG)Driver.class: $(MAINPROG_DEPS)
	$(JCC) -classpath $(JAVA_CUP_RUNTIME):$(SRC_DIR) $(SRC_DIR)/$(PROG)Driver.java

