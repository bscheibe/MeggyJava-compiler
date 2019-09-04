package symtable;
import java.util.*;
import ast.node.*;

import exceptions.InternalException;

public class VarSTE extends STE 
{

    public IType type;
    public String base;
    public int offset;

    public VarSTE(){

    }
    public VarSTE(String name, IType type,String base, int offset){
        this.name = name;
        this.type = type;
        this.base = base;
        this.offset = offset;

    }
}