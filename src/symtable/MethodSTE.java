package symtable;
import java.util.*;
import ast.node.*;

import exceptions.InternalException;

public class MethodSTE extends STE{

    public LinkedList<Formal> params;
    public LinkedList<VarDecl> varDeclList;
    public IType returnType;
    public int size;

    public MethodSTE(){

    }

    public MethodSTE(String name, LinkedList<Formal> params,LinkedList<VarDecl> varDeclList, IType returnType,int size){
        this.name = name;
        this.params = params;
        this.varDeclList = varDeclList;
        this.returnType = returnType;
        this.size = size;

    }
}