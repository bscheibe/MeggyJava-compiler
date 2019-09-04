package symtable;
import java.util.*;
import ast.node.*;

import exceptions.InternalException;

public class Scope{

    private String cls;
    private String method;

    public Scope(){


    }
    public Scope(String cls, String method){
        this.cls = cls;
        this.method = method;
    }

    public String getScope(){

        return cls+method;
    }
    public void setCls(String cls){
        this.cls = cls;
    }

    public String getCls() {
        return cls;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}