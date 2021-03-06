# MeggyJava Compiler

This was a joint project with Jeffrey Lort for the University of Delaware's Compilers course, with the goal of building a compiler for MeggyJava, a subset of the Java programming language. 

Our file structure has all tests in the /Testing directory organized by project. PA3/PA4/PA5 test cases are in their respective subdirectories, and test different phases of extending the compiler's features.

## Building with Make
Running 'make' will produce an MJ.jar compiler and copy it, along with the MJSIM.jar (simulator for MeggyJava's JRE) and reference_compiler.jar (a documented existing compiler), into all the testing subdiretories. 
Running 'make three' 'make four' or 'make five' will automatically run the MJ.jar compiler against all .java test cases in that subdirectory. Running some cases on MJSIM may require use of the -j flag.
'make clean' is updated to remove all compiled .class and .jar files, as well as any assembly or AST-related files that stem from testing. 

## Deliverables
	DONE: Add PA5 grammar rules to our parser.
	DONE: Update to the visitors to handle new features.
	DONE: Have ST Builder annotate instance and local variables to include offset and base information.
	DONE: Type check for objects, instance variables, and local variables.
	DONE: Extend assembly code generation to include PA3 features.
	DONE: Extend addembly code generation to include PA4 features.
	Extend addembly code generation to include PA5 features.
	DONE: Produce errors for doubly defined symbols.
		ex: [23,16] Redefined symbol foo

### Remaining code geneation needed for final grammar layer: 
	DONE: Ability to pass 'this' in parameters and expressions where appropriate.
	Assignment operator for class types.
	DONE: Implement the New operator for class types.
	Local variable uses and definitions.
	Class references and comparisons between them.
