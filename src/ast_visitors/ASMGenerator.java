package ast_visitors;

import ast.node.*;
import ast.visitor.DepthFirstVisitor;

import java.util.Stack;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

import symtable.*;
import symtable.Type;
import exceptions.InternalException;
import exceptions.SemanticException;

public class ASMGenerator extends DepthFirstVisitor {
    private int ifLabelNumber = 0;
    private int elseLabelNumber = 0;
    private int whileLabelNumber = 0;
    private int equalsLabelNumber = 0;
    private int ltLabelNumber = 0;
    private int andExpLabelNumber = 0;
    private int buttonLabel = 0;
    private int callCount = 0;
    private int currentOffset = 0;
    private String button = "";
    private Stack<Integer> ifStack;
    private Stack<Integer> elseStack;
    private Stack<Integer> whileStack;
    private java.io.PrintStream asmOut;
    private Scope scope;
    private CheckTypes ct;

    public ASMGenerator(String filename, CheckTypes ct) {
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
        this.scope = new Scope();
        this.ct = ct;
    }

    public void defaultOut(Node node) {
        /*System.err.println("Node not implemented in AVRGen, " + node.getClass());*/
    }
    public void outAndExp(AndExp node)
    {
        andExpLabelNumber++;

    }
    /*TODO: double check this*/
    @Override
    public void visitAndExp(AndExp node) {
        asmOut.println("#AND expression start");
        inAndExp(node);
        assert (node.getLExp() != null);
        node.getLExp().accept(this);
        asmOut.println("#AND expression Left");
        asmOut.println("\tldi r25,0");
        asmOut.println("\ttst r24" +
                "\n\tbreq falseAnd" + andExpLabelNumber);
        assert (node.getRExp() != null);

        node.getRExp().accept(this);
        asmOut.println("#AND expression Right");

        asmOut.println("\ttst r24" +
                "\n\tbreq falseAnd" + andExpLabelNumber +
                "\n\tldi r24,1");
        asmOut.println("jmp andContinue"+andExpLabelNumber);

        asmOut.println("falseAnd" + andExpLabelNumber + ":" +
                "\n\tldi r24,0" );
        asmOut.println("andContinue" + andExpLabelNumber + ":");
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
        asmOut.println("\tldi r25,0");
        asmOut.println();

    }

    public void outCallExp(CallExp node) {
        callCount++;
        //ct.methodTable.get(ct.classNameMap.get(node.getExp()));
        asmOut.println("\tcall " + ct.classNameMap.get(node.getExp())+node.getId());
        //return val in r24:25
        asmOut.println("\tldi r20," + (node.getArgs().size()+2));
        asmOut.println("\tpop r28");
        asmOut.println("\tpop r29");
        //pop off the args that were pushed before the calls
        asmOut.println("POP" + node.getId() + callCount + ":");
        asmOut.println("\tpop r12");
        asmOut.println("\tpop r12");
        asmOut.println("\tsub r20,r21");
        asmOut.println("\ttst r20");
        asmOut.println("\tbrne POP" + node.getId() + callCount);
     /*   for(int i = 0; i < node.getArgs().size(); i++){
            asmOut.println("\tpop r12");
            asmOut.println("\tpop r12");
        }*/

    }

    @Override
    public void visitCallExp(CallExp node) {
        inCallExp(node);
        assert node.getExp() != null : "Code generator-callexp exp null";

        node.getExp().accept(this);
	//asmOut.println("push	r25\npush	r24");
        LinkedList<MethodSTE> methodSTELinkedList =
                ct.methodTable.get(ct.classNameMap.get(node.getExp()));

         if(methodSTELinkedList == null) {
            methodSTELinkedList = ct.methodTable.get(ct.classTypeMap.get(((ILiteral)node.getExp()).getLexeme())); 
        }

        MethodSTE callSTE = null;
        for (MethodSTE ste : methodSTELinkedList) {
            //System.out.println(ste.name +" compared to "+node.getId());
            if (ste.name.equals(node.getId())) {
                //System.out.println("\t true");
                //this will only work for methods returning a primitive type
                //since i haven't figured out custom types properly
                //this.mCurrentST.setExpType(node, getType(ste.returnType));
                callSTE = ste;
            }
        }
        assert callSTE != null : "CodeGen - callstatement couldn't find methodSTE";


        List<IExp> copy = new ArrayList<IExp>(node.getArgs());
        for (IExp e : copy) {
            e.accept(this);
            //all args are 2 bytes
            asmOut.println("\tpush r25");
            asmOut.println("\tpush r24");

        }
        asmOut.println("\tpush r29");
        asmOut.println("\tpush r28");
        asmOut.println("\tin r28,__SP_L__");
        asmOut.println("\tin r29,__SP_H__");
        outCallExp(node);
    }

    public void outCallStatement(CallStatement node) {
        callCount++;
        asmOut.println("\tcall " + ct.classNameMap.get(node.getExp())+node.getId());
        //return val in r24:25
        asmOut.println("\tldi r20," + (node.getArgs().size() + 1));
        //pop off the args that were pushed before the calls
        asmOut.println("\tpop r28");
        asmOut.println("\tpop r29");
/*        for(int i = 0; i < node.getArgs().size(); i++){
            asmOut.println("\tpop r12");
            asmOut.println("\tpop r12");
        }*/
       asmOut.println("POP" + node.getId() + callCount + ":");
        asmOut.println("\tpop r12");
        asmOut.println("\tpop r12");
        asmOut.println("\tsub r20,r21");
        asmOut.println("\ttst r20");
        asmOut.println("\tbrne POP" + node.getId() + callCount);


    }

    @Override
    public void visitCallStatement(CallStatement node) {
        inCallStatement(node);
        assert node.getExp() != null : "Code generator-callStatement exp null";

        node.getExp().accept(this);

        LinkedList<MethodSTE> methodSTELinkedList =
                ct.methodTable.get(ct.classNameMap.get(node.getExp()));

        if(methodSTELinkedList == null) {
            methodSTELinkedList = ct.methodTable.get(ct.classTypeMap.get(((ILiteral)node.getExp()).getLexeme())); 
        }
        MethodSTE callSTE = null;
        for (MethodSTE ste : methodSTELinkedList) {
            //System.out.println(ste.name +" compared to "+node.getId());
            if (ste.name.equals(node.getId())) {
                //System.out.println("\t true");
                //this will only work for methods returning a primitive type
                //since i haven't figured out custom types properly
                //this.mCurrentST.setExpType(node, getType(ste.returnType));
                callSTE = ste;
            }
        }
        assert callSTE != null : "CodeGen - callstatement couldn't find methodSTE";


        List<IExp> copy = new ArrayList<IExp>(node.getArgs());
        for (IExp e : copy) {
            e.accept(this);
            asmOut.println("\tpush r25");
            asmOut.println("\tpush r24");
        }
        asmOut.println("\tpush r29");
        asmOut.println("\tpush r28");
        asmOut.println("\tin r28,__SP_L__");
        asmOut.println("\tin r29,__SP_H__");

        outCallStatement(node);
    }

    public void outChildClassDecl(ChildClassDecl node) {
        defaultOut(node);
    }

    public void outColorExp(ColorLiteral node) {
        asmOut.println("\t#ColorLit");
        //also gets two bytes so that == can pop two values off the stack no matter what
        asmOut.println("\tldi r25,0");
        asmOut.println("\tldi r24," + node.getIntValue());
        asmOut.println();
    }

    public void outEqualExp(EqualExp node) {
        // asmOut.println("#Equals");
        //asmOut.println("\tpop r25 \n    pop r24 \n    cp r24,r25");

/*        asmOut.println("\tpop    r18 " +
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
                "\ncontinue" + equalsLabelNumber + ":");*/


        equalsLabelNumber++;
        asmOut.println();
    }

    @Override
    public void visitEqualExp(EqualExp node) {
        asmOut.println("#Equals");
        inEqualExp(node);
        assert node.getLExp() != null : "Code generator-equal left exp null";
        node.getLExp().accept(this);
        //save result of lexp, which should be 24,25
        asmOut.println("\tmov r26,r24");
        asmOut.println("\tmov r27,r25");


        assert node.getLExp() != null : "Code generator-equal right exp null";

        node.getRExp().accept(this);
        //rexp in r24,25
        asmOut.println("\tcp  r24,r26 ");
        asmOut.println("\tbrne returnFalse" + equalsLabelNumber);
        asmOut.println("\tcpc r25,r27");
        asmOut.println("\tbrne returnFalse" + equalsLabelNumber);
        //result of exp in 24
        asmOut.println("\tldi r24,1");
        asmOut.println("\tjmp continue" + equalsLabelNumber);
        asmOut.println("\treturnFalse" + equalsLabelNumber + ":");
        asmOut.println("\tldi r24,0");
        asmOut.println("\tcontinue" + equalsLabelNumber + ":");
        outEqualExp(node);
    }

    public void outFalseExp(FalseLiteral node) {
        asmOut.println("\t#FALSE");
        asmOut.println("\tldi r24," + node.getIntValue());
        asmOut.println("\tldi r25,0");

    }

    public void outFormal(Formal node) {
        defaultOut(node);
    }

    public void outIdLiteral(IdLiteral node) {
        assert ct.varTable.get(scope.getScope()) != null : scope.getScope() + " scope not found in varTable";
        LinkedList<VarSTE> varSTELinkedList = ct.varTable.get(scope.getScope());
        VarSTE varSTE = null;
        for (VarSTE ste : varSTELinkedList) {
            if(ste.name.equals(node.getLexeme())){
                varSTE = ste;
            }
        }
        LinkedList<VarSTE> classVarSTELinkedList = ct.varTable.get(scope.getCls());
        for (VarSTE ste : classVarSTELinkedList) {
            if(ste.name.equals(node.getLexeme())){
                varSTE = ste;
            }
        }
        assert varSTE != null: node.getLexeme() + " entry not found in varTable";
        asmOut.println("\tldd r25, " + varSTE.base +" + "+(varSTE.offset+2));
        asmOut.println("\tldd r24, "+ varSTE.base +" + "+(varSTE.offset+1));

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


    }



    @Override
    public void visitIfStatement(IfStatement node) {
        inIfStatement(node);
        assert node.getExp() != null : "Code Generator- If statement exp null";
        node.getExp().accept(this);
        //result of exp should be in 24,25
        asmOut.println("\ttst r24");
        asmOut.println("\tbreq endThenBlock" + (ifLabelNumber));

        assert (node.getThenStatement() != null);

        node.getThenStatement().accept(this);
        if (node.getElseStatement() != null) {
            //jump past the else statement
            elseLabelNumber = elseStack.pop();
            asmOut.println("jmp endElseBlock" + elseLabelNumber);
        }
        asmOut.println("endThenBlock" + ifStack.pop() + ":");


        if (node.getElseStatement() != null) {
            node.getElseStatement().accept(this);
            asmOut.println("endElseBlock" + elseLabelNumber + ":");
        }
        outIfStatement(node);
    }

    public void outIntegerExp(IntLiteral node) {

        asmOut.println("\t#IntLit");
        asmOut.println("\tldi r24,lo8(" + node.getIntValue() + ")");
        asmOut.println("\tldi r25,hi8(" + node.getIntValue() + ") ");
        asmOut.println();
    }

    public void outLengthExp(LengthExp node) {
        defaultOut(node);
    }

    public void outLtExp(LtExp node) {
        ltLabelNumber++;
    }
    @Override
    public void visitLtExp(LtExp node)
    {
        asmOut.println("#LT start");
        inLtExp(node);
        boolean compareBytes = false;

        if(node.getLExp() != null)
        {
            node.getLExp().accept(this);
            asmOut.println("#LT left");
            asmOut.println("\tmov r26,r24");
            asmOut.println("\tmov r27,r25");

        }
        if(node.getRExp() != null)
        {
            node.getRExp().accept(this);
            if(ct.mCurrentST.getExpType(node.getLExp()) == Type.BYTE && ct.mCurrentST.getExpType(node.getRExp()) == Type.BYTE){
                asmOut.println("#LT right");
                asmOut.println("\tcp  r26,r24 ");
                asmOut.println("\tbrlt returnTrueLT" + ltLabelNumber);
                //result of exp in 24
                asmOut.println("\tldi r24,0");
                asmOut.println("\tjmp continueLT" + ltLabelNumber);
                asmOut.println("\treturnTrueLT" + ltLabelNumber + ":");
                asmOut.println("\tldi r24,1");
                asmOut.println("\tcontinueLT" + ltLabelNumber + ":");
            }else{
                asmOut.println("#LT right");
                asmOut.println("\tcp  r26,r24 ");
                asmOut.println("\tcpc r27,r25");
                asmOut.println("\tbrlt returnTrueLT" + ltLabelNumber);
                //result of exp in 24
                asmOut.println("\tldi r24,0");
                asmOut.println("\tjmp continueLT" + ltLabelNumber);
                asmOut.println("\treturnTrueLT" + ltLabelNumber + ":");
                asmOut.println("\tldi r24,1");
                asmOut.println("\tcontinueLT" + ltLabelNumber + ":");
            }



        }
        outLtExp(node);
    }
    public void inMainClass(MainClass node) {

        asmOut.println();
    }
    public void outMainClass(MainClass node) {

        asmOut.println(
                //"\n\tpop r20 " +
                "endLabel: " +
                        "\n\tjmp endLabel " +
                        "\n\tret " +
                        "\n\t.size   main, .-main");
       // asmOut.println("\t.size   main, .-main");
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
                "\n\tldi r24,1" +
                "\n\tjmp buttonContinue" + buttonLabel +
                "\nbuttonFalseLabel" + buttonLabel + ":" +
                "\n\tldi r24,0" +
                "\nbuttonContinueLabel" + buttonLabel + ":"
        );


        asmOut.println();
    }

    public void outMeggyDelay(MeggyDelay node) {
        asmOut.println("\t#Delay");
        //result of delay exp should be in 24,25
        asmOut.println("\n\tcall _Z8delay_msj");
        asmOut.println();


    }

    public void outMeggyGetPixel(MeggyGetPixel node) {
        asmOut.println("\t#GetPixel");
        asmOut.println("\tcall _Z6ReadPxhh");//0 padding
    }

    public void outMeggySetAuxLEDs(MeggySetAuxLEDs node) {
        defaultOut(node);


    }

    public void outMeggySetPixel(MeggySetPixel node) {


/*        asmOut.println("\tpop r20 " +
                "\n\tpop r25 " + //pops the zero
                "\n\tpop r22 " +
                "\n\tpop r25 " + //pops the zero
                "\n\tpop r24 " +
                "\n\tpop r25 " + //pops the zero
                "\n\tcall _Z6DrawPxhhh " +
                "\n\tcall _Z12DisplaySlatev");*/
        asmOut.println();
    }

    public void visitMeggySetPixel(MeggySetPixel node) {
        asmOut.println("\t#SetPixel");
        inMeggySetPixel(node);
        assert node.getXExp() != null : "CodeGen - setpixel Xexp is null";
        node.getXExp().accept(this);
        //save 24
        asmOut.println("\tmov r18,r24");


        assert node.getYExp() != null : "CodeGen - setpixel Yexp is null";

        node.getYExp().accept(this);
        asmOut.println("\tmov r19,r24");


        assert node.getColor() != null : "CodeGen - setpixel ColorExp is null";

        node.getColor().accept(this);
        //color is in 24 already, move the others to the correct regs
        asmOut.println("\tmov r20,r24");
        asmOut.println("\tmov r22,r19");
        asmOut.println("\tmov r24,r18");
        asmOut.println("\tcall _Z6DrawPxhhh");
        asmOut.println("\tcall _Z12DisplaySlatev");
        outMeggySetPixel(node);

    }

    public void outMeggyToneStart(MeggyToneStart node) {



    }

    public void visitMeggyToneStart(MeggyToneStart node)
    {
        inMeggyToneStart(node);
        assert node.getToneExp() != null : "Codegen tonestart toneExp node is null";

            node.getToneExp().accept(this);
            asmOut.println("mov r26,r24");
            asmOut.println("mov r27,r25");


        assert node.getDurationExp() != null : "Codegen tonestart duration node is null";

            node.getDurationExp().accept(this);
           //I think the right things are in the right registers at this point
            //duration in 22:23
            //tone in 24:25

        asmOut.println("mov r22,r24");
        asmOut.println("mov r23,r25");
        asmOut.println("mov r24,r26");
        asmOut.println("mov r25,r27");
        asmOut.println("call _Z10Tone_Startjj");
        outMeggyToneStart(node);
    }


    public void inMethodDecl(MethodDecl node) {
        currentOffset = 1;
        scope.setMethod(node.getName());
        asmOut.println("\t.text");
        asmOut.println(".global "+scope.getScope());
        asmOut.println("\t.type "+scope.getScope()+", @function");
        asmOut.println(scope.getScope() + ":");
    }

    public void outMethodDecl(MethodDecl node) {
        currentOffset = 1;
        assert node != null : "MethodDecl node is null";

        asmOut.println("\tret");
        asmOut.println("\t.size "+scope.getScope()+", .-"+scope.getScope());
        scope.setMethod("");
    }
    @Override
    public void visitMethodDecl(MethodDecl node)
    {
        inMethodDecl(node);
        if(node.getType() != null)
        {
            node.getType().accept(this);
        }
        {
            int offset = 2 * node.getFormals().size();
            for(Formal e : node.getFormals())
            {
                //find in varste list,update offset, then in idlit move from the offset to 24:25
                assert ct.varTable.get(scope.getScope()) != null : scope.getScope() + " MethodDecl scope not found in varTable";
                LinkedList<VarSTE> varSTELinkedList = ct.varTable.get(scope.getScope());
                VarSTE varSTE = null;
                for (VarSTE ste : varSTELinkedList) {
                    if(ste.name.equals(e.getName())){
                        varSTE = ste;
                    }
                }
                assert varSTE != null: node.getName() + " entry not found in varTable";
                varSTE.offset = offset;
                //offset is 2 bytes since each formal is two bytes
                offset -= 2;
                e.accept(this);
            }
        }
        {
            List<VarDecl> copy = new ArrayList<VarDecl>(node.getVarDecls());
            for(VarDecl e : copy)
            {
                e.accept(this);
            }
        }
        {
            List<IStatement> copy = new ArrayList<IStatement>(node.getStatements());
            for(IStatement e : copy)
            {
                e.accept(this);
            }
        }
        if(node.getExp() != null)
        {
            node.getExp().accept(this);
        }
        outMethodDecl(node);
    }
    public void outMinusExp(MinusExp node) {

/*        asmOut.println("\tpop    r18" +
                "\n\tpop    r19" +

                "\n\tpop    r24" +
                "\n\tpop    r25" +


                "\n\tsub    r24, r18" +
                "\n\tsbc    r25, r19" +

                "\n\tpush   r25" +
                "\n\tpush   r24");
        asmOut.println();*/

    }

    @Override
    public void visitMinusExp(MinusExp node) {
        asmOut.println("#MINUS");
        inMinusExp(node);
        assert node.getLExp() != null : "Codegen - minus lexp is null";
        node.getLExp().accept(this);
        //result of lexp in 24 25, save
        asmOut.println("\tmov r26,r24");
        asmOut.println("\tmov r27,r25");
        assert node.getRExp() != null : "Codegen - minus rexp is null";
        node.getRExp().accept(this);

        asmOut.println("\tsub r26,r24");
        asmOut.println("\tsbc r27,r25");
        asmOut.println("\tmov r24,r26");
        asmOut.println("\tmov r25,r27");

        //result still in 24,25
        outMinusExp(node);
    }

    public void outMulExp(MulExp node) {
/*        asmOut.println("\t#Multiply");
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
                "\n\teor    r1,r1");*/


    }

    @Override
    public void visitMulExp(MulExp node) {
        inMulExp(node);
        asmOut.println("\t#Multiply");
        assert node.getLExp() != null : "Codegen - mul lexp is null";
        node.getLExp().accept(this);
        //result of lexp in 24 25, save
        asmOut.println("\tmov r26,r24");
        //asmOut.println("\tmov r27,r25");
        assert node.getRExp() != null : "Codegen - mul rexp is null";
        node.getRExp().accept(this);
        asmOut.println("\tmuls r24,r26");
        //put result in my exp output regs
        asmOut.println("\tmov r24,r0");
        asmOut.println("\tmov r25,r1");
        //gotta do this for some reason
        asmOut.println("\teor r1,r1");
        asmOut.println("\teor r0,r0");

        outMulExp(node);
    }

    public void outNewArrayExp(NewArrayExp node) {
        defaultOut(node);
    }

    public void outNewExp(NewExp node) {
        int size = -1;
        LinkedList<ClassSTE> classList = ct.classTable.get("Global");
        for (ClassSTE ste : classList) {
            if (ste.name.equals(node.getId())) {
                size = ste.getSize();
            }
        }
        assert size >= 0 : "Class size not found";
        asmOut.println("# NewExp");
        asmOut.println("ldi r24, lo8("+size+")");
        asmOut.println("ldi r25, hi8("+size+")");
        asmOut.println("call  malloc");
        asmOut.println("# Push object address");
        asmOut.println("push r24");
        asmOut.println("push r25");
    }

    public void outNegExp(NegExp node) {
        asmOut.println("\t#Negate");

        asmOut.println("\n\tldi r26, 0");
        asmOut.println("\n\tldi r27, 0");
        asmOut.println("\tsub r26, r24 ");
        asmOut.println("\tsbc r27, r25 ");
        //result in 24:25
        asmOut.println("\n\tmov r24,r26");
        asmOut.println("\n\tmov r25,r27");
    }

    public void outNotExp(NotExp node) {
        //and's the 8-bit value with 11111110
        //gets the inverse of that
        //so the com instruction isn't supported by the sim, use eor instead
        asmOut.println("#NOT");
        asmOut.println("\tldi r26,254" +
                "\n\tor r24,r26" +
                "\n\tldi r26,255" +
                "\n\teor r24,r26");
    }

    public void outPlusExp(PlusExp node) {
/*        asmOut.println("#PLUS");
        asmOut.println("\tpop    r18" +
                "\n\tpop    r19" +

                "\n\tpop    r24" +
                "\n\tpop    r25" +


                "\n\tadd    r24, r18" +
                "\n\tadc    r25, r19" +

                "\n\tpush   r25" +
                "\n\tpush   r24");
        asmOut.println();*/
    }

    @Override
    public void visitPlusExp(PlusExp node) {
        inPlusExp(node);
        asmOut.println("\t#PLUS");
        assert node.getLExp() != null : "Codegen - plus lexp is null";
        node.getLExp().accept(this);
        //result of lexp in 24 25, save
        asmOut.println("\tmov r26,r24");
        asmOut.println("\tmov r27,r25");
        assert node.getRExp() != null : "Codegen - plus rexp is null";
        node.getRExp().accept(this);
        asmOut.println("\tadd r24,r26");
        asmOut.println("\tadc r25,r27");

        outPlusExp(node);
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
        //r21 is a constant for 1.
        asmOut.println("\tldi r21,1");
        scope.setCls("Global");
    }

    public void outProgram(Program node) {



    }

    public void outThisExp(ThisLiteral node) {

        asmOut.println("# This literal handle");
        asmOut.println("ldd r31, Y + 2");
        asmOut.println("ldd r30, Y + 1");
        asmOut.println("push r31");
        asmOut.println("push r30");
    }

    public void inTopClassDecl(TopClassDecl node) {

        scope.setCls(node.getName());
    }

    public void outTopClassDecl(TopClassDecl node) {
        scope.setCls("Global");
    }


    public void outToneExp(ToneLiteral node) {
        asmOut.println("\t#ToneLit");
        asmOut.println("\tldi r25,hi8(" + node.getIntValue()+")");
        asmOut.println("\tldi r24,lo8(" + node.getIntValue()+")");
        asmOut.println();
    }



    public void outTrueExp(TrueLiteral node) {
        asmOut.println("\t#TrueLit");
        asmOut.println("\tldi r24," + node.getIntValue());
        asmOut.println("\tldi r25,0");
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
        asmOut.println("\t tst r24");
        asmOut.println("\tbreq endWhileStatement" + whileLabelNumber);

        assert (node.getStatement() != null);
        node.getStatement().accept(this);
        asmOut.println("jmp beginWhile" + whileLabelNumber);
        asmOut.println("endWhileStatement" + whileStack.pop() + ":");

        outWhileStatement(node);
    }
}
