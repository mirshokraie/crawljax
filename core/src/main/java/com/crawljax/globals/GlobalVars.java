package com.crawljax.globals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import  com.crawljax.core.state.*;
import com.crawljax.graph.WeightedGraph;

public class GlobalVars {
	
	/* treeMap<function name, [[uniqueId,statevertex,eventable,eventType],[],...]> function name is the handler of the element and uniqueId is the id
	 * of the target element which has been given to the element through our code, and eventable is the event associated with the element to
	 * be later used for the path from initial state to the current state, eventType can be click,hover,unbind,... */
	/* function name is of type string, uniqueId is of type string, stateVertex is of type string, eventable is of type Eventable, eventType is of type string>*/
	public static TreeMap<String,ArrayList<ArrayList<Object>>> eventableElementsMap=new TreeMap<String, ArrayList<ArrayList<Object>>>();
	
	public static HashSet<String> executedFuncList=new HashSet<String>();;
	public static WeightedGraph dynamicCallGraph=new WeightedGraph();
	public static  HashMap<String,String> labeledFunctions=new HashMap<String,String>();
	public static WeightedGraph staticCallGraph=new WeightedGraph();
	public static  HashMap<String,HashSet<String>> potentialFutrueClickables=new HashMap<String,HashSet<String>>();
	

}
