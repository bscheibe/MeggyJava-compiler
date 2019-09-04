/*
 * MJDriver.java
 *
 * usage: 
 *   java MJ [ --two-pass-interpret | --two-pass-mips ] infile
 *
 * This driver either calls a MiniJava interpreter or generates MIPS
 * code to execute the MiniJava program.
 * The default is the interpreter.
 *
 */
import java.io.*;

import mjparser.*;
import ast_visitors.*;
import ast.node.*;
import ast.visitor.*;
import symtable.*;

public class MJDriver {

      private static void usage() {
          System.err.println(
            "MJ: Specify input file in program arguments");
      }
     
      public static void main(String args[]) 
      {
        
        if(args.length < 1)
        {         
            usage();
            System.exit(1);
        }

        // filename should be the last command line option
        String filename = args[args.length-1];

        try {
          // construct the lexer, 
          // the lexer will be the same for all of the parsers
          Yylex lexer = new Yylex(new FileReader(filename));

          // create the parser
          mj parser = new mj(lexer);
          int lastInPath = filename.lastIndexOf('/');
          parser.programName = filename.substring(lastInPath+1);
          System.out.println("Driver finds input filename: " + parser.programName);

          // and parse
          ast.node.Node ast_root = (ast.node.Node)parser.parse().value;

          // Print ast to file.
          java.io.PrintStream astout =
            new java.io.PrintStream(
                new java.io.FileOutputStream(filename + ".ast.dot"));
          ast_root.accept(new DotVisitor(new PrintWriter(astout)));
          System.out.println("Printing AST to " + filename + ".ast.dot");
          
          // create the symbol table
         
          DeclarationTable dt = new DeclarationTable();

            ast_root.accept(dt);
            CheckTypes ct = new CheckTypes(new SymTable(),dt);
            ast_root.accept(ct);

            java.io.PrintStream avrsout =
                    new java.io.PrintStream(
                            new java.io.FileOutputStream(filename + ".s"));
            ast_root.accept(new ASMGenerator(filename,ct));
            System.out.println("Printing Atmel assembly to " + filename + ".s");


         /*
          STBuilder stVisitor = new STBuilder();
          ast_root.accept(stVisitor);
          symtable.SymTable globalST = stVisitor.getSymTable();
          
          
          // print symbol table to file
          java.io.PrintStream STout =
            new java.io.PrintStream(
                new java.io.FileOutputStream(filename + ".ST.dot"));
          System.out.println("Printing symbol table to " + filename + ".ST.dot");
          globalST.outputDot(STout);
              
          
          // perform type checking 
          ast_root.accept(new CheckTypes(globalST));
          
          /*
          // Determine whether to do register allocation or not.
          if ( args.length == 2 && args[0].equals("--regalloc") ) {
              // trying out register allocation
              AVRregAlloc regVisitor = new AVRregAlloc(globalST);
              ast_root.accept(regVisitor);
              
              // print info about temps to a file
              java.io.PrintStream asttempout =
                  new java.io.PrintStream(
                      new java.io.FileOutputStream(filename + ".ast.temp.dot"));
                ast_root.accept(new DotVisitorWithMap(new PrintWriter(asttempout),
                        regVisitor.getTempMap()));
                System.out.println("Printing AST to " + filename + ".ast.temp.dot");
            
          } else {
            // determine how to layout variables in AVR program
            ast_root.accept(new AVRallocVars(globalST));
          }
          
          
          // Create assembly file. 
          java.io.PrintStream avrsout =
              new java.io.PrintStream(
                      new java.io.FileOutputStream(filename + ".s"));

          // Generate the prologue. 
          System.out.println("Generate prolog using avrH.rtl.s");
          InputStream mainPrologue=null;
          BufferedReader reader=null;
          try {
            mainPrologue = new FileInputStream("./Testing/avrH.rtl.s");
              reader = new BufferedReader(new InputStreamReader(mainPrologue));

              String line = null;
              while ((line = reader.readLine()) != null) {
                avrsout.println(line);
              }
          } catch ( Exception e2) {
            e2.printStackTrace();
          } finally{
              try{
                if (mainPrologue!=null) mainPrologue.close();
                if (reader!=null) reader.close();
              } catch (IOException e) {
                e.printStackTrace();
              }
          }
          
          // Generate AVR code that evaluates the program.
          ast_root.accept(new AVRgenVisitor(avrsout));
          System.out.println("Printing Atmel assembly to " + filename + ".s");

          // Generate the epilogue. 
          System.out.println("Generate epilogue using avrF.rtl.s");
          InputStream mainEpilogue=null;
          reader=null;
          try {
            mainEpilogue = new FileInputStream("./Testing/avrF.rtl.s");
              reader = new BufferedReader(new InputStreamReader(mainEpilogue));

              String line = null;
              while ((line = reader.readLine()) != null) {
                avrsout.println(line);
              }
          } catch ( Exception e2) {
            e2.printStackTrace();
          } finally{
              try{
                if (mainEpilogue!=null) mainEpilogue.close();
                if (reader!=null) reader.close();
              } catch (IOException e) {
                e.printStackTrace();
              }
          }
          avrsout.flush();
          */
        } catch(exceptions.SemanticException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }  
      }

}
