package ast_visitors;

/**
 * CheckTypes
 * <p>
 * This AST visitor traverses a MiniJava Abstract Syntax Tree and checks
 * for a number of type errors.  If a type error is found a SymanticException
 * is thrown
 * <p>
 * CHANGES to make next year (2012)
 * - make the error messages between *, +, and - consistent <= ??
 * <p>
 * Bring down the symtab code so that it only does get and set Type
 * for expressions
 */

import ast.node.*;
import ast.visitor.DepthFirstVisitor;

import java.util.*;

import symtable.*;
import symtable.SymTable;
import symtable.Type;
import exceptions.InternalException;
import exceptions.SemanticException;

//I'll also be checking usages
public class CheckTypes extends DepthFirstVisitor {

    public SymTable mCurrentST;
    //keys are ClassMethod or Class (for class variables, only have "this" now)
    public HashMap<String, LinkedList<VarSTE>> varTable;
    //keys are Class names
    public HashMap<String, LinkedList<MethodSTE>> methodTable;
    //key is "Global"
    public HashMap<String, LinkedList<ClassSTE>> classTable;
    private Scope scope;
    public HashMap<Node, String> classNameMap;
    public HashMap<String, String> classTypeMap;

    public CheckTypes(SymTable st, DeclarationTable dt) {
        if (st == null) {
            throw new InternalException("unexpected null argument");
        }
        mCurrentST = st;
        this.varTable = dt.getVarTable();
        this.methodTable = dt.getMethodTable();
        this.classTable = dt.getClassTable();
        this.classNameMap = new HashMap<Node, String>();
        this.scope = new Scope();
        this.classTypeMap = new HashMap<String, String>();
    }

    //========================= Overriding the visitor interface
    public Type getType(IType node) {
        if (node instanceof BoolType) {
            return Type.BOOL;
        } else if (node instanceof ButtonType) {
            return Type.BUTTON;
        } else if (node instanceof ByteType) {
            return Type.BYTE;
        } else if (node instanceof ClassType) {
            return Type.CLASSTYPE;
        } else if (node instanceof ColorType) {
            return Type.COLOR;
        } else if (node instanceof IntType) {
            return Type.INT;
        } else if (node instanceof IntArrayType) {

        } else if (node instanceof ToneType) {
            return Type.TONE;
        } else if (node instanceof VoidType) {
            return Type.VOID;
        }
        return null;

    }

    public void defaultOut(Node node) {
        /*System.err.println("Node not implemented in CheckTypes, " + node.getClass());*/
    }


    public void outAndExp(AndExp node) {
        assert node.getLExp() != null : "And LExp is null";
        assert node.getRExp() != null : "And RExp is null";
        if (this.mCurrentST.getExpType(node.getLExp()) != Type.BOOL) {
            throw new SemanticException(
                    "Invalid left operand type for operator &&",
                    node.getLExp().getLine(), node.getLExp().getPos());
        }


        if (this.mCurrentST.getExpType(node.getRExp()) != Type.BOOL) {
            throw new SemanticException(
                    "Invalid right operand type for operator &&",
                    node.getRExp().getLine(), node.getRExp().getPos());
        }

        this.mCurrentST.setExpType(node, Type.BOOL);
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

    public void outBlockStatement(BlockStatement node) {
        defaultOut(node);
    }

    public void outButtonExp(ButtonLiteral node) {
        assert node != null : "ButtonLit exp was null";
        this.mCurrentST.setExpType(node, Type.BUTTON);
    }

    public void outByteCast(ByteCast node) {
        assert node.getExp() != null : "Byte cast exp was null";
        Type expType = this.mCurrentST.getExpType(node.getExp());
        if (expType == Type.BYTE || expType == Type.INT) {
            this.mCurrentST.setExpType(node, Type.BYTE);
        } else {
            throw new SemanticException(
                    expType + ": Invalid type for byte cast",
                    node.getExp().getLine(),
                    node.getExp().getPos());
        }

    }

    public void outCallExp(CallExp node) {
        Type expType = this.mCurrentST.getExpType(node.getExp());
        assert expType == Type.CLASSTYPE : "CallExp not made with a class type";
        //assert expType.classType.length() > 0 : "Class type not set";
        LinkedList<MethodSTE> methodSTELinkedList;
        methodSTELinkedList = methodTable.get(classNameMap.get(node.getExp()));
        if(methodSTELinkedList == null) {
            methodSTELinkedList = methodTable.get(classTypeMap.get(((ILiteral)node.getExp()).getLexeme())); 
        }
        if(methodSTELinkedList == null) {
            throw new SemanticException(
                    node.getId() + ": Can't resolve scope",
                    node.getExp().getLine(),
                    node.getExp().getPos());
        }
        MethodSTE callSTE = null;
        for (MethodSTE ste : methodSTELinkedList) {
            //System.out.println(ste.name +" compared to "+node.getId());
            if (ste.name.equals(node.getId())) {
                //System.out.println("\t true");
                //this will only work for methods returning a primitive type
                //since i haven't figured out custom types properly
                this.mCurrentST.setExpType(node, getType(ste.returnType));
                callSTE = ste;
            }


        }
        if (callSTE == null) {
            throw new SemanticException(
                    node.getId() + ": symbol not found",
                    node.getExp().getLine(),
                    node.getExp().getPos());
        }

        List<IExp> copy = new ArrayList<IExp>(node.getArgs());
        //System.out.println(callSTE.name);
        if (copy.size() != callSTE.params.size()) {
            throw new SemanticException(
                    node.getId() + ": wrong number of arguments",
                    node.getExp().getLine(),
                    node.getExp().getPos());
        } else {
            //System.out.println(node.getId()+ " call with "+copy.size() +" args equals " +callSTE.name+ " with " +callSTE.params.size()+" args");
        }

        for (int i = 0; i < callSTE.params.size(); i++) {
            //Formal formal  = (Formal) copy.get(i);
            //System.out.println(this.mCurrentST.getExpType(copy.get(i)));
            if (this.mCurrentST.getExpType(copy.get(i)) != getType(callSTE.params.get(i).getType())) {
                throw new SemanticException(
                        "Arg does not match type for " + callSTE.params.get(i).getName(),
                        node.getExp().getLine(),
                        node.getExp().getPos());

            }
        }
        //node.getId() method name
        //now we have the ste
        //node.getArgs()
/*        IExp caller = node.getExp();
        if(caller instanceof NewExp){
            MethodSTE ste = methodTable.get(caller.getId());
            System.out.println("CallExp newexp "+ste.name);
        }*/
        //check that each of the args matches
        //check the scope of the
        //this only will work for new Class.method(), will fix for later versions


    }

    public void outCallStatement(CallStatement node) {
        Type expType = this.mCurrentST.getExpType(node.getExp());
        assert expType == Type.CLASSTYPE : "CallStatement not made with a class type";
        //assert expType.classType.length() > 0 : "Class type not set";
        LinkedList<MethodSTE> methodSTELinkedList;

        // System.out.println(classNameMap.get(node.getExp()));
        methodSTELinkedList = methodTable.get(classNameMap.get(node.getExp()));
        if(methodSTELinkedList == null) {
            methodSTELinkedList = methodTable.get(classTypeMap.get(((ILiteral)node.getExp()).getLexeme())); 
        }
        if(methodSTELinkedList == null) {
            throw new SemanticException(
                    node.getId() + ": Can't resolve scope",
                    node.getExp().getLine(),
                    node.getExp().getPos());
        }

        MethodSTE callSTE = null;
        for (MethodSTE ste : methodSTELinkedList) {
            //System.out.println(ste.name +" compared to "+node.getId());
            if (ste.name.equals(node.getId())) {
                //System.out.println("\t true");
                //this will only work for methods returning a primitive type
                //since i haven't figured out custom types properly
                this.mCurrentST.setExpType(node, getType(ste.returnType));
                callSTE = ste;
            }


        }
        if (callSTE == null) {
            throw new SemanticException(
                    node.getId() + ": symbol not found",
                    node.getExp().getLine(),
                    node.getExp().getPos());
        }

        List<IExp> copy = new ArrayList<IExp>(node.getArgs());
        //System.out.println(callSTE.name);
        if (copy.size() != callSTE.params.size()) {
            throw new SemanticException(
                    node.getId() + ": wrong number of arguments",
                    node.getExp().getLine(),
                    node.getExp().getPos());
        } else {
            //System.out.println(node.getId()+ " call with "+copy.size() +" args equals " +callSTE.name+ " with " +callSTE.params.size()+" args");
        }

        for (int i = 0; i < callSTE.params.size(); i++) {
           // System.out.println(this.mCurrentST.getExpType(copy.get(i)));

            if (this.mCurrentST.getExpType(copy.get(i)) != getType(callSTE.params.get(i).getType())) {
                throw new SemanticException(
                        "Arg does not match type for " + callSTE.params.get(i).getName(),
                        node.getExp().getLine(),
                        node.getExp().getPos());

            }
        }
    }

    public void outChildClassDecl(ChildClassDecl node) {
        defaultOut(node);
    }

    public void outColorExp(ColorLiteral node) {
        assert node != null : "ColorLit node is null";
        this.mCurrentST.setExpType(node, Type.COLOR);
    }

    public void outEqualExp(EqualExp node) {
        assert node.getLExp() != null : "Equal LExp is null";
        assert node.getRExp() != null : "Equal RExp is null";
        Type lexpType = this.mCurrentST.getExpType(node.getLExp());
        Type rexpType = this.mCurrentST.getExpType(node.getRExp());

        if ((lexpType == Type.COLOR && rexpType == Type.COLOR) ||
                (lexpType == Type.INT && rexpType == Type.INT) ||
                (lexpType == Type.BYTE && rexpType == Type.BYTE) ||
                (lexpType == Type.INT && rexpType == Type.BYTE) ||
                (lexpType == Type.BYTE && rexpType == Type.INT)) {
            this.mCurrentST.setExpType(node, Type.BOOL);
        } else {
            throw new SemanticException(
                    "Cannot compare " + lexpType + " and " + rexpType,
                    node.getLExp().getLine(),
                    node.getLExp().getPos());
        }
    }

    public void outFalseExp(TrueLiteral node) {
        assert node != null : "FalseLit node is null";
        this.mCurrentST.setExpType(node, Type.BOOL);

    }

    public void outFormal(Formal node) {
        defaultOut(node);
    }

    public void outIdLiteral(IdLiteral node) {
        LinkedList<VarSTE> methodSteList = varTable.get(scope.getScope());
        for (VarSTE ste : methodSteList) {
            if (ste.name.equals(node.getLexeme())) {
                mCurrentST.setExpType(node, getType(ste.type));
                return;
            }
        }
        LinkedList<VarSTE> classSteList = varTable.get(scope.getCls());
        for (VarSTE ste : classSteList) {
            if (ste.name.equals(node.getLexeme())) {
                mCurrentST.setExpType(node, getType(ste.type));
                return;
            }
        }
        throw new SemanticException(
                node.getLexeme() + " : symbol not found in Class scope " + scope.getCls() + " or Method Scope " + scope.getMethod(),
                node.getLine(),
                node.getPos());


    }

    public void outIfStatement(IfStatement node) {
        defaultOut(node);
    }

    public void outIntegerExp(IntLiteral node) {
        assert node != null : "IntLit node is null";
        this.mCurrentST.setExpType(node, Type.INT);
    }

    public void outLengthExp(LengthExp node) {
        defaultOut(node);
    }

    public void outLtExp(LtExp node) {
        assert node.getLExp() != null : "Lt LExp is null";
        assert node.getRExp() != null : "Lt RExp is null";
        Type lexpType = this.mCurrentST.getExpType(node.getLExp());
        Type rexpType = this.mCurrentST.getExpType(node.getRExp());
        if ((lexpType == Type.INT && rexpType == Type.INT) ||
                (lexpType == Type.BYTE && rexpType == Type.BYTE) ||
                (lexpType == Type.INT && rexpType == Type.BYTE) ||
                (lexpType == Type.BYTE && rexpType == Type.INT)) {
            this.mCurrentST.setExpType(node, Type.BOOL);
        } else {
            throw new SemanticException(
                    "Cannot compare " + lexpType + " and " + rexpType,
                    node.getLExp().getLine(),
                    node.getLExp().getPos());
        }
    }

    public void outMainClass(MainClass node) {
        defaultOut(node);
    }

    public void outMeggyCheckButton(MeggyCheckButton node) {
        Type expType = this.mCurrentST.getExpType(node.getExp());
        if (expType == Type.BUTTON) {
            this.mCurrentST.setExpType(node, Type.BOOL);
        } else {
            throw new SemanticException(
                    "MeggyCheckButton takes a BUTTON",
                    node.getExp().getLine(),
                    node.getExp().getPos());
        }
    }

    public void outMeggyDelay(MeggyDelay node) {
        assert node.getExp() != null : "MeggyDelay exp is null";
        Type expType = this.mCurrentST.getExpType(node.getExp());
        if (expType == Type.BYTE || expType == Type.INT) {
            this.mCurrentST.setExpType(node, Type.VOID);
        } else {
            throw new SemanticException(
                    "Cannot call MeggyDelay with a " + expType,
                    node.getExp().getLine(),
                    node.getExp().getPos());
        }
    }

    public void outMeggyGetPixel(MeggyGetPixel node) {
        assert node.getXExp() != null : "getPixel X exp is null";
        assert node.getYExp() != null : "getPixel Y exp is null";
        Type expTypeX = this.mCurrentST.getExpType(node.getXExp());
        Type expTypeY = this.mCurrentST.getExpType(node.getYExp());
        if (expTypeX == Type.BYTE && expTypeY == Type.BYTE) {
            this.mCurrentST.setExpType(node, Type.COLOR);
        } else {
            throw new SemanticException(
                    "Operands to MeggyGetPixel function must be BYTE, BYTE",
                    node.getXExp().getLine(),
                    node.getXExp().getPos());
        }
    }

    public void outMeggySetAuxLEDs(MeggySetAuxLEDs node) {
        defaultOut(node);
    }

    public void outMeggySetPixel(MeggySetPixel node) {
        assert node.getXExp() != null : "setPixel X exp is null";
        assert node.getYExp() != null : "setPixel y exp is null";
        assert node.getColor() != null : "setPixel color exp is null";
        Type expTypeX = this.mCurrentST.getExpType(node.getXExp());
        Type expTypeY = this.mCurrentST.getExpType(node.getYExp());
        Type expTypeColor = this.mCurrentST.getExpType(node.getColor());
        if ((expTypeX == Type.BYTE && expTypeY == Type.BYTE) &&
                expTypeColor == Type.COLOR
        ) {
            this.mCurrentST.setExpType(node, Type.VOID);
        } else {
            throw new SemanticException(
                    "Operands to MeggySetPixel function must be BYTE, BYTE, COLOR",
                    node.getXExp().getLine(),
                    node.getXExp().getPos());
        }
    }

    public void outMeggyToneStart(MeggyToneStart node) {
        defaultOut(node);
    }

    public void inMethodDecl(MethodDecl node) {

        scope.setMethod(node.getName());
    }

    public void outMethodDecl(MethodDecl node) {
        assert node != null : "MethodDecl node is null";
        scope.setMethod("");
    }

    public void outMinusExp(MinusExp node) {
        assert node.getLExp() != null : "Minus LExp is null";
        assert node.getRExp() != null : "Minus RExp is null";
        Type lexpType = this.mCurrentST.getExpType(node.getLExp());
        Type rexpType = this.mCurrentST.getExpType(node.getRExp());
        if ((lexpType == Type.INT || lexpType == Type.BYTE) &&
                (rexpType == Type.INT || rexpType == Type.BYTE)
        ) {
            this.mCurrentST.setExpType(node, Type.INT);
        } else {
            throw new SemanticException(
                    "Cannot subtract " + lexpType + " and " + rexpType,
                    node.getLExp().getLine(),
                    node.getLExp().getPos());
        }
    }

    public void outMulExp(MulExp node) {
        assert node.getLExp() != null : "Multiply LExp is null";
        assert node.getRExp() != null : "Multiply RExp is null";
        Type lexpType = this.mCurrentST.getExpType(node.getLExp());
        Type rexpType = this.mCurrentST.getExpType(node.getRExp());
        if ((lexpType == Type.BYTE) &&
                (rexpType == Type.BYTE)
        ) {
            this.mCurrentST.setExpType(node, Type.INT);
        } else {
            throw new SemanticException(
                    "Cannot multiply " + lexpType + " and " + rexpType,
                    node.getLExp().getLine(),
                    node.getLExp().getPos());
        }
    }

    public void outNewArrayExp(NewArrayExp node) {
        defaultOut(node);
    }

    public void outNewExp(NewExp node) {
        LinkedList<ClassSTE> steLinkedList = classTable.get("Global");
        for (ClassSTE ste : steLinkedList) {
            if (ste.name.equals(node.getId())) {
                this.mCurrentST.setExpType(node, Type.CLASSTYPE);
                this.classNameMap.put(node,node.getId());
                //this.mCurrentST.setExpType(node, new Type(node.getId()));
                return;
            }
        }
        throw new SemanticException(
                node.getId() + " : symbol not found",
                node.getLine(),
                node.getPos());
    }

    public void outNegExp(NegExp node) {
        assert (node.getExp() != null);
        Type expType = this.mCurrentST.getExpType(node.getExp());
        if (expType == Type.INT || expType == Type.BYTE) {
            this.mCurrentST.setExpType(node, Type.INT);
        } else {
            throw new SemanticException(
                    "Cannot negate type " + expType,
                    node.getExp().getLine(),
                    node.getExp().getPos());
        }
    }

    public void outNotExp(NotExp node) {
        assert node.getExp() != null : "Not Exp is null";
        Type expType = this.mCurrentST.getExpType(node.getExp());
        if (expType == Type.BOOL) {
            this.mCurrentST.setExpType(node, Type.BOOL);
        } else {
            throw new SemanticException(
                    "Cannot apply ! operator to " + expType,
                    node.getExp().getLine(),
                    node.getExp().getPos());
        }
    }

    public void outPlusExp(PlusExp node) {
        assert node.getLExp() != null : "Plus LExp is null";
        assert node.getRExp() != null : "Plus RExp is null";
        Type lexpType = this.mCurrentST.getExpType(node.getLExp());
        Type rexpType = this.mCurrentST.getExpType(node.getRExp());
        if ((lexpType == Type.INT || lexpType == Type.BYTE) &&
                (rexpType == Type.INT || rexpType == Type.BYTE)
        ) {
            this.mCurrentST.setExpType(node, Type.INT);
        } else {
            throw new SemanticException(
                    "Cannot add " + lexpType + " and " + rexpType,
                    node.getLExp().getLine(),
                    node.getLExp().getPos());
        }

    }

    public void outProgram(Program node) {
        defaultOut(node);
    }

    public void inProgram(Program node) {
        scope.setCls("Global");
    }

    public void outThisExp(ThisLiteral node) {
        assert scope.getCls().length() >0 : "(this) not inside of class scope";
        //this.mCurrentST.setExpType(node, new Type(scope.getCls()));
        this.mCurrentST.setExpType(node, Type.CLASSTYPE);
        this.classNameMap.put(node,scope.getCls());
    }

    public void outToneExp(ToneLiteral node) {
        defaultOut(node);
    }

    public void inTopClassDecl(TopClassDecl node) {
        scope.setCls(node.getName());
    }

    public void outTopClassDecl(TopClassDecl node) {
        scope.setCls("Global");
    }

    public void outTrueExp(TrueLiteral node) {
        assert node != null : "TrueLit node is null";
        this.mCurrentST.setExpType(node, Type.BOOL);

    }

    public void outVarDecl(VarDecl node) {
        if (node.getType() instanceof ClassType) {
            classTypeMap.put(node.getName(), ((ClassType)node.getType()).getName());
        }
    }

    public void outWhileStatement(WhileStatement node) {
        defaultOut(node);
    }

}
