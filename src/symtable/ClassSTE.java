package symtable;
import java.util.*;
import ast.node.*;

import exceptions.InternalException;

public class ClassSTE extends STE{

	private int size;
    public int location;

    public ClassSTE(){
    	this.size = 0;
    }
    public ClassSTE(String name){
        this.name = name;
    }

    public int getSize() {
    	return size;
    }

    public void upSize() {
    	size += 2;
    }
}