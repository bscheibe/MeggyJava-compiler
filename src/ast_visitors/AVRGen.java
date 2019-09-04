package ast_visitors;

import ast.node.*;
import ast.visitor.DepthFirstVisitor;

import java.util.Stack;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

import symtable.SymTable;
import symtable.Type;
import exceptions.InternalException;
import exceptions.SemanticException;

public class AVRGen extends DepthFirstVisitor {
    private int ifLabelNumber = 0;
    private int elseLabelNumber = 0;
    private int whileLabelNumber = 0;
    private int equalsLabelNumber = 0;
    private int andExpLabelNumber = 0;
    private int buttonLabel = 0;
    private String button = "";
    private Stack<Integer> ifStack;
    private Stack<Integer> elseStack;
    private Stack<Integer> whileStack;
    private java.io.PrintStream asmOut;

    public AVRGen(String filename) {
        try {
            asmOut = new java.io.PrintStream(
                    new java.io.FileOutputStream(filename + ".s"));


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        this.ifStack = new Stack<Integer>();
        this.whileStack = new Stack<Integer>();
        this.elseStack = new Stack<Integer>();
    }

    public void defaultOut(Node node) {
        /*System.err.println("Node not implemented in AVRGen, " + node.getClass());*/
    }

    @Override
    public void visitAndExp(AndExp node) {
        inAndExp(node);
        assert (node.getLExp() != null);
        node.getLExp().accept(this);
        asmOut.println("\tpop r24 " +
                "\n\t tst r24" +
                "\n\tbreq falseAnd" + andExpLabelNumber);
        assert (node.getRExp() != null);
        node.getRExp().accept(this);
        asmOut.println("\tpop r24 " +
                "\n\t tst r24" +
                "\n\tbreq falseAnd" + andExpLabelNumber +
                "\n\tldi r20,1" +
                "\n\tpush r20");

        asmOut.println("\tfalseAnd" + andExpLabelNumber + ":" +
                "\n\tldi r20,0" +
                "\n\tpush r20");
        outAndExp(node);
    }

    public void outArrayAssignStatement(ArrayAssignStatement node) {
        defaultOut(node);
    }

    public void outArrayExp(ArrayExp node) {
        defaultOut(node);
    }

    public void outAssignStatement(AssignStatement node) {
        defaultOut(node);
    }


    public void inBlockStatement(BlockStatement node) {
        //node.blockNumber = labelNumber++;
        //node.asmStartLabel = "StartBlock"+node.blockNumber;
        //asmOut.println("StartBlock"+node.blockNumber+":");
    }

    public void outBlockStatement(BlockStatement node) {
        //node.asmEndLabel   = "EndBlock" +node.blockNumber;
        //asmOut.println("EndBlock" +node.blockNumber+ ":");
    }

    public void outButtonExp(ButtonLiteral node) {
        int buttonVal = node.getIntValue();

        switch (buttonVal) {
            case 1:
                button = "Button_B";
                break;
            case 2:
                button = "Button_A";
                break;
            case 4:
                button = "Button_Up";
                break;
            case 8:
                button = "Button_Down";
                break;
            case 16:
                button = "Button_Left";
                break;
            case 32:
                button = "Button_Right";
                break;

        }
    }

    public void outByteCast(ByteCast node) {
        //Push zero so that all operations deal with the same size operands for numeric values,
        //which makes the stack simpler
        asmOut.println("\t#ByteCast");
        asmOut.println("\tpop r24 " +
                "\n\tpop r25 " +
                "\n\tldi r20,0" +
                "\n\tpush r20" +
                "\n\tpush r24");
        asmOut.println();

    }

    public void outCallExp(CallExp node) {
        defaultOut(node);
    }

    public void outCallStatement(CallStatement node) {
        defaultOut(node);
    }

    public void outChildClassDecl(ChildClassDecl node) {
        defaultOut(node);
    }

    public void outColorExp(ColorLiteral node) {
        asmOut.println("\t#ColorLit");
        //also gets two bytes so that == can pop two values off the stack no matter what
        asmOut.println("\n\tldi r20,0" +
                "\n\tpush r20");
        asmOut.println("\tldi r20," + node.getIntValue());
        asmOut.println("\tpush r20");
        asmOut.println();
    }

    public void outEqualExp(EqualExp node) {
        asmOut.println("#Equals");
        //asmOut.println("\tpop r25 \n    pop r24 \n    cp r24,r25");

        asmOut.println("\tpop    r18 " +
                "\n\tpop    r19 " +
                "\n\tpop    r24 " +
                "\n\tpop    r25 " +
                "\n\tcp    r24, r18 " +
                "\n\tbrne   returnFalse" + equalsLabelNumber +
                "\n\tcpc   r25, r19" +
                "\n\tbrne   returnFalse" + equalsLabelNumber +
                "\n\tldi r20,1" +
                "\n\tpush r20" +
                "\n\tjmp continue" + equalsLabelNumber +
                "\nreturnFalse" + equalsLabelNumber + ":" +
                "\n\tldi r20,0" +
                "\n\tpush r20" +
                "\ncontinue" + equalsLabelNumber + ":");


        equalsLabelNumber++;
        asmOut.println();
    }

    public void outFalseExp(FalseLiteral node) {
        asmOut.println("\t#FALSE");
        asmOut.println("\tldi r20," + node.getIntValue());
        asmOut.println("\tpush r20");
    }

    public void outFormal(Formal node) {
        defaultOut(node);
    }

    public void outIdLiteral(IdLiteral node) {
        defaultOut(node);
    }

    public void inIfStatement(IfStatement node) {

        ifLabelNumber++;
        ifStack.push(ifLabelNumber);
        asmOut.println("\t#IN IfStatement " + ifLabelNumber);

        if (node.getElseStatement() != null) {
            //jump past the else statement
            elseLabelNumber++;
            elseStack.push(elseLabelNumber);
        }

        //IStatement thenStatement = node.getThenStatement();
        //asmOut.println("\tpop r25");
        //asmOut.println("\tbrne EndBlock"+(labelNumber+1));
        //asmOut.println();
       /* if(node.getElseStatement() ! = null){
            BlockStatement elseStatement = node.getElseStatement();
        }else{

        }*/

    }

    public void outIfStatement(IfStatement node) {
        //asmOut.println("\t#OUT IfStatement " + ifLabelNumber);
        //IStatement thenStatement = node.getThenStatement();
        //asmOut.println("\tpop r25");
        //asmOut.println("\tbrne "+thenStatement.asmEndLabel);
       /* if(node.getElseStatement() ! = null){
            BlockStatement elseStatement = node.getElseStatement();
        }else{

        }*/

    }

    @Override
    public void visitIfStatement(IfStatement node) {
        inIfStatement(node);
        assert (node.getExp() != null);
        node.getExp().accept(this);
        asmOut.println("\tpop r25");
        asmOut.println("\ttst r25");
        asmOut.println("\tbreq endThenBlock" + (ifLabelNumber));

        assert (node.getThenStatement() != null);

        node.getThenStatement().accept(this);
        if (node.getElseStatement() != null) {
            //jump past the else statement
            asmOut.println("jmp endElseBlock" + elseLabelNumber);
        }
        asmOut.println("endThenBlock" + ifStack.pop() + ":");


        if (node.getElseStatement() != null) {
            node.getElseStatement().accept(this);
            asmOut.println("endElseBlock" + elseStack.pop() + ":");
        }
        outIfStatement(node);
    }

    public void outIntegerExp(IntLiteral node) {

        asmOut.println("\t#IntLit");
        asmOut.println("\tldi r24,lo8(" + node.getIntValue() + ")" +
                " \n\tldi r25,hi8(" + node.getIntValue() + ") " +
                "\n\tpush r25 " +
                "\n\tpush r24");
        asmOut.println();
    }

    public void outLengthExp(LengthExp node) {
        defaultOut(node);
    }

    public void outLtExp(LtExp node) {
        defaultOut(node);
    }

    public void inMainClass(MainClass node) {

        asmOut.println();
    }

    public void inMeggyCheckButton(MeggyCheckButton node) {
        buttonLabel++;
    }

    public void outMeggyCheckButton(MeggyCheckButton node) {

        asmOut.println("\t#Check button");
        asmOut.println("\tcall _Z16CheckButtonsDownv " +
                "\n\tlds r24," + button +
                "\n\ttst  r24" +
                "\n\tbreq  buttonFalseLabel" + buttonLabel +
                "\n\tldi r20,1" +
                "\n\tpush r20" +
                "\n\tjmp buttonContinue" + buttonLabel +
                "\nbuttonFalseLabel" + buttonLabel + ":" +
                "\n\tldi r20,0" +
                "\n\tpush r20" +
                "\nbuttonContinueLabel" + buttonLabel + ":"
        );


        asmOut.println();
    }

    public void outMeggyDelay(MeggyDelay node) {
        asmOut.println("\t#Delay");
        asmOut.println(
                "\tpop r24 " +
                        "\n\tpop r25 " +
                        "\n\tcall _Z8delay_msj");
        asmOut.println();


    }

    public void outMeggyGetPixel(MeggyGetPixel node) {
        asmOut.println("\t#GetPixel");
        asmOut.println("\tpop r22 " +
                "\n\tpop r25 " + //pops the zero
                "\n\tpop r24 " +
                "\n\tpop r25 " + //pops the zero
                "\n\tcall _Z6ReadPxhh"+
                "\n\tpush r24 " +
                "\n\tpush r25 ");//0 padding
    }

    public void outMeggySetAuxLEDs(MeggySetAuxLEDs node) {
        defaultOut(node);


    }

    public void outMeggySetPixel(MeggySetPixel node) {

        asmOut.println("\t#SetPixel");
        asmOut.println("\tpop r20 " +
                "\n\tpop r25 " + //pops the zero
                "\n\tpop r22 " +
                "\n\tpop r25 " + //pops the zero
                "\n\tpop r24 " +
                "\n\tpop r25 " + //pops the zero
                "\n\tcall _Z6DrawPxhhh " +
                "\n\tcall _Z12DisplaySlatev");
        asmOut.println();
    }

    public void outMeggyToneStart(MeggyToneStart node) {
        defaultOut(node);
    }

    public void outMethodDecl(MethodDecl node) {
        defaultOut(node);
    }

    public void outMinusExp(MinusExp node) {
        asmOut.println("#MINUS");
        asmOut.println("\tpop    r18" +
                "\n\tpop    r19" +

                "\n\tpop    r24" +
                "\n\tpop    r25" +


                "\n\tsub    r24, r18" +
                "\n\tsbc    r25, r19" +

                "\n\tpush   r25" +
                "\n\tpush   r24");
        asmOut.println();

    }

    public void outMulExp(MulExp node) {
        asmOut.println("\t#Multiply");
        asmOut.println("\tpop r18 " +
                "\n\tpop    r25 " + //extra 0
                "\n\tpop    r22 " +
                "\n\tpop    r25 " + //extra 0
                "\n\tmov    r24, r18 " +
                "\n\tmov    r26, r22 " +
                "\n\tmuls   r24, r26 " +
                "\n\tpush   r1 " +
                "\n\tpush   r0 " +
                "\n\teor    r0,r0 " +
                "\n\teor    r1,r1");


    }

    public void outNewArrayExp(NewArrayExp node) {
        defaultOut(node);
    }

    public void outNewExp(NewExp node) {
        defaultOut(node);
    }

    public void outNegExp(NegExp node) {
        asmOut.println("\t#Negate");
        asmOut.println("\tpop    r24" +
                "\n\tpop r25" +
                "\n\tldi r22, 0" +
                "\n\tldi r23, 0" +
                "\n\tsub r22, r24 " +
                "\n\tsbc r23, r25 " +
                "\n\tpush r23" +
                "\n\tpush   r22");
    }

    public void outNotExp(NotExp node) {
        //and's the 8-bit value with 11111110
        //gets the inverse of that
        //so the com instruction isn't supported by the sim, use eor instead
        asmOut.println("#NOT");
        asmOut.println("\tpop r17" +
                "\n\tldi r18,254" +
                "\n\tor r17,r18" +
                "\n\tldi r18,255" +
                "\n\teor r17,r18" +
                "\n\tpush r17");
    }

    public void outPlusExp(PlusExp node) {
        asmOut.println("#PLUS");
        asmOut.println("\tpop    r18" +
                "\n\tpop    r19" +

                "\n\tpop    r24" +
                "\n\tpop    r25" +


                "\n\tadd    r24, r18" +
                "\n\tadc    r25, r19" +

                "\n\tpush   r25" +
                "\n\tpush   r24");
        asmOut.println();
    }

    public void inProgram(Program node) {
        asmOut.println("\t.file  \"main.java\" " +
                "\n__SREG__ = 0x3f " +
                "\n__SP_H__ = 0x3e" +
                "\n__SP_L__ = 0x3d " +
                "\n__tmp_reg__ = 0 " +
                "\n__zero_reg__ = 1 " +
                "\n\t.global __do_copy_data" +
                "\n\t.global __do_clear_bss " +
                "\n\t.text " +
                "\n.global main " +
                "\n\t.type   main, @function " +
                "\nmain: " +
                "\n\tpush r29 " +
                "\n\tpush r28 " +
                "\n\tin r28,__SP_L__ " +
                "\n\tin r29,__SP_H__ " +
                "\n\tcall _Z18MeggyJrSimpleSetupv");
        asmOut.println();
    }

    public void outProgram(Program node) {
        asmOut.println(
                //"\n\tpop r20 " +
                        "endLabel: " +
                        "\n\tjmp endLabel " +
                        "\n\tret " +
                        "\n\t.size   main, .-main");


    }

    public void outThisExp(ThisLiteral node) {
        defaultOut(node);
    }

    public void outToneExp(ToneLiteral node) {
        asmOut.println("\t#ToneLit");
        asmOut.println("\tldi r20," + node.getIntValue());
        asmOut.println("\tpush r20");
        asmOut.println();
    }

    public void outTopClassDecl(TopClassDecl node) {
        defaultOut(node);
    }

    public void outTrueExp(TrueLiteral node) {
        asmOut.println("\t#TrueLit");
        asmOut.println("\tldi r20," + node.getIntValue());
        asmOut.println("\tpush r20");
        asmOut.println();
    }

    public void outVarDecl(VarDecl node) {
        defaultOut(node);
    }

    public void inWhileStatement(WhileStatement node) {

        whileLabelNumber++;
        whileStack.push(whileLabelNumber);
        asmOut.println("beginWhile" + whileLabelNumber + ":");

    }

    @Override
    public void visitWhileStatement(WhileStatement node) {
        inWhileStatement(node);
        assert (node.getExp() != null);
        node.getExp().accept(this);
        asmOut.println("\tpop    r18" +
                "\n\t tst r18" +
                "\n\tbreq endWhileStatement" + whileLabelNumber);

        assert (node.getStatement() != null);
        node.getStatement().accept(this);
        asmOut.println("jmp beginWhile" + whileLabelNumber);
        asmOut.println("endWhileStatement" + whileStack.pop() + ":");

        outWhileStatement(node);
    }
}