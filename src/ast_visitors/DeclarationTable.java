package ast_visitors;

import ast.node.*;
import ast.visitor.DepthFirstVisitor;

import java.util.*;

import symtable.*;
import symtable.Type;
import exceptions.InternalException;
import exceptions.SemanticException;


public class DeclarationTable extends DepthFirstVisitor {
    //scopeID, identifer,its STE
    private HashMap<String, LinkedList<VarSTE>> varTable = new HashMap<String, LinkedList<VarSTE>>();
    private HashMap<String, LinkedList<MethodSTE>> methodTable = new HashMap<String, LinkedList<MethodSTE>>();
    private HashMap<String, LinkedList<ClassSTE>> classTable = new HashMap<String, LinkedList<ClassSTE>>();
    private Scope scope;

    public DeclarationTable() {
        scope = new Scope("Global", "");
    }

    public void inVarDecl(VarDecl node)
    {
    	LinkedList<VarSTE> varSTELinkedList = varTable.get(scope.getScope());
        for (VarSTE ste: varSTELinkedList){

            if(ste.name.equals(node.getName())){
                throw new SemanticException(
                        "Redefined symbol "+node.getName(),
                        node.getLine(),
                        node.getPos());
            }
        }
        
        LinkedList<ClassSTE> classSteList = classTable.get("Global");
            for (ClassSTE ste : classSteList) {
                if (ste.name.equals(scope)) {
                    varSTELinkedList.add(new VarSTE(node.getName(), node.getType(),"Z", ste.getSize()));
                    ste.upSize();
                    return;
                }
            }

        varSTELinkedList.add(new VarSTE(node.getName(), node.getType(),"Y", 0));
    }


    public void inFormal(Formal node) {
        assert varTable.get(scope.getScope()) != null : "varTable for "+ scope.getScope()+ " is not set up";
        LinkedList<VarSTE> varSTELinkedList = varTable.get(scope.getScope());
        for (VarSTE ste: varSTELinkedList){

            if(ste.name.equals(node.getName())){
                throw new SemanticException(
                        "Redefined symbol "+node.getName(),
                        node.getLine(),
                        node.getPos());
            }
        }
        varSTELinkedList.add(new VarSTE(node.getName(), node.getType(),"Y", 0));
        //varTable.put(scope)

    }

    public HashMap<String, LinkedList<ClassSTE>> getClassTable() {
        return classTable;
    }

    public HashMap<String, LinkedList<MethodSTE>> getMethodTable() {
        return methodTable;
    }

    public HashMap<String, LinkedList<VarSTE>> getVarTable() {
        return varTable;
    }


    public void inMethodDecl(MethodDecl node) {
        //System.out.println(scope.getScope());
        LinkedList<MethodSTE> methodList = methodTable.get(scope.getScope());
        String classScope = scope.getScope();
        //System.out.println(methodList.size());

            for (MethodSTE ste : methodList) {
                if (ste.name.equals(node.getName())) {
                    throw new SemanticException(
                            "Method "+node.getName() + " redefined",
                            node.getLine(),
                            node.getPos());
                }
            }


        //make ste list for vars
        //new VarSTE(node.getName(), node.getType(), "Y", 1));
        //System.out.println("method " + node.getName());
        methodList.add(new MethodSTE(node.getName(), node.getFormals(),
                node.getVarDecls(),node.getType(),1));
       // methodTable.put(scope.getScope(), entry);
        //set scope for things in method
        scope.setMethod(node.getName());
        varTable.put(scope.getScope(), new LinkedList<VarSTE>());
        //System.out.println("methodtable size " + methodTable.size());

    }



    public void outMethodDecl(MethodDecl node) {
        scope.setMethod("");
    }


    public void inTopClassDecl(TopClassDecl node) {
        assert classTable.get("Global") != null: "Global scope has not been set up";

        LinkedList<ClassSTE> classList = classTable.get("Global");
        for (ClassSTE ste : classList) {
            if (ste.name.equals(node.getName())) {
                throw new SemanticException(
                        "Class "+node.getName() + " redefined",
                        node.getLine(),
                        node.getPos());
            }
        }
        classList.add(new ClassSTE(node.getName()));
        scope.setCls(node.getName());
        scope.setMethod("");
        //start a new method and var lists for this scope
        methodTable.put(scope.getScope(),new LinkedList<MethodSTE>() );
        LinkedList<VarSTE> entry = new LinkedList<VarSTE>();
        //VarSTE(String name, IType type,String base, int offset)
       // IType classType = ;
        entry.add(new VarSTE("this",new ClassType(node.getLine(),node.getPos(),node.getName()),"Y",2));
        varTable.put(scope.getScope(),entry);
        //LinkedList<MethodSTE> entry = new LinkedList<MethodSTE>();


    }


    public void outTopClassDecl(TopClassDecl node) {
        scope.setCls("Global");
    }

    @Override
    public void visitTopClassDecl(TopClassDecl node) {
        inTopClassDecl(node);
        {
            List<VarDecl> copy = new ArrayList<VarDecl>(node.getVarDecls());
            for (VarDecl e : copy) {
                e.accept(this);
            }
        }
        {
            List<MethodDecl> copy = new ArrayList<MethodDecl>(node.getMethodDecls());
            for (MethodDecl e : copy) {
                e.accept(this);
            }
        }
        outTopClassDecl(node);
    }

    public void inProgram(Program node) {

        LinkedList<ClassSTE> entry = new LinkedList<ClassSTE>();
        classTable.put("Global", entry);

    }

    public void outProgram(Program node) {
    }


}