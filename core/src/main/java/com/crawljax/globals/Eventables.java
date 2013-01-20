package com.crawljax.globals;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import  com.crawljax.core.state.*;;

public class Eventables {
	
	/* treeMap<function name, (uniqueId,eventable)> function name is the handler of the element and uniqueId is the id
	 * of the target element which has been given to the element through our code, and eventable is the event associated with the element to
	 * be later used for the path from initial state to the current state */
	/* function name is of type string, uniqueId is of type string, eventable is of type Eventable)>*/
	public static TreeMap<String,ArrayList<Object>> eventableElementsMap=new TreeMap<String, ArrayList<Object>>();
	

}
