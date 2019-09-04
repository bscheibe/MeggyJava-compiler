This was a joint project with Jeffrey Lort for the University of Delaware's Compilers course. 

Our file structure has all tests in the /Testing directory organized by project. PA3/PA4/PA5 test cases are in their respective subdirectories, and test different phases of extending the compiler's features.

Running 'make' will produce an MJ.jar compiler and copy it, along with the MJSIM.jar and reference_compiler.jar, into all the testing subdiretories. 
Running 'make three' 'make four' or 'make five' will automatically run the MJ.jar compiler on all .java test cases in that subdirectory. Running some cases on MJSIM may require use of the -j option.
'make clean' is updated to remove all .class files and the .jar files from the testing directories. 

Deliverables:
DONE: Add PA5 grammar rules to our parser.
DONE: Update to the visitors to handle new features.
DONE: Have ST Builder annotate instance and local variables to include offset and base information.
DONE: Type check for objects, instance variables, and local variables.
DONE: Extend .java.s file generation to include PA3 features.
DONE: Extend .java.s file generation to include PA4 features.
Extend .java.s file generation to include PA5 features.
DONE: Produce errors for doubly defined symbols.
	ex: [23,16] Redefined symbol foo

Remaining code geneation needed for PA5 grammar: 
	DONE: this in parameters and expression
	Assignment
	DONE: New class objects
	Local variable uses and definitions
	Equality comparison of class refs
	Class refs
