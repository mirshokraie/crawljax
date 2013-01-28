package com.crawljax.staticTracer;

import java.util.ArrayList;
import java.util.List;


import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.PropertyGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawljaxController;
import com.crawljax.globals.StaticCallGraph;
import com.crawljax.graph.Edge;
import com.crawljax.graph.Vertex;


public class StaticFunctionTracer implements NodeVisitor {
	
	private static List<String> functionCallsNotToLog=new ArrayList<String>();
	public static  List<String> functionNodes=new ArrayList<String>();
	
	private boolean shouldTrackFunctionCalls;
	private boolean shouldTrackFunctionNodes;
	protected static final Logger LOGGER = LoggerFactory.getLogger(CrawljaxController.class.getName());
	 /**
	  *  This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;
	
	/**
	 * @param scopeName
	 *            the scopeName to set
	 */
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}
	
	public void setShouldTrackFuncCalls_Nodes(boolean shouldTrackfuncCalls, boolean shouldTrackfuncNodes){
		shouldTrackFunctionCalls=shouldTrackfuncCalls;
		shouldTrackFunctionNodes=shouldTrackfuncNodes;
	}


	/**
	 * @return the scopeName
	 */
	public String getScopeName() {
		return scopeName;
	}
	
	/**
	 * Parse some JavaScript to a simple AST.
	 * 
	 * @param code
	 *            The JavaScript source code to parse.
	 * @return The AST node.
	 */
	public AstNode parse(String code) {
		Parser p = new Parser(compilerEnvirons, null);
		return p.parse(code, null, 0);
		
	}
	
	public StaticFunctionTracer(){
		

		
		functionCallsNotToLog.add("parseInt");
		functionCallsNotToLog.add("jQuery");
		functionCallsNotToLog.add("setTimeout");
		functionCallsNotToLog.add("$");
		functionCallsNotToLog.add(".css");
		functionCallsNotToLog.add(".addClass");
		functionCallsNotToLog.add(".click");
		functionCallsNotToLog.add(".unbind");
		functionCallsNotToLog.add("Math.");
		functionCallsNotToLog.add(".append");
		functionCallsNotToLog.add(".attr");
		functionCallsNotToLog.add(".random");
		functionCallsNotToLog.add("push");
		functionCallsNotToLog.add(".split");
		functionCallsNotToLog.add("v");
		functionCallsNotToLog.add("send(new Array(");
		functionCallsNotToLog.add("new Array(");
		functionCallsNotToLog.add("btoa");
		functionCallsNotToLog.add("atob");
		functionCallsNotToLog.add("atob");
	}
	
	private boolean shouldVisitFunctionCall(FunctionCall function){
		if (functionCallsNotToLog.size()==0)
			return true;
		for (String funcName:functionCallsNotToLog){
			
			if (function.getTarget().toSource().contains(funcName)){
				return false;
			}
		}
		return true;
	}
	
	protected String getFunctionName(FunctionNode f) {
		if (f==null)
			return "NoFunctionNode";
	/*	else if(f.getParent() instanceof LabeledStatement){
			return ((LabeledStatement)f.getParent()).shortName();
		}
	*/	else if(f.getParent() instanceof ObjectProperty){
			return ((ObjectProperty)f.getParent()).getLeft().toSource();
		}
		Name functionName = f.getFunctionName();

		if (functionName == null) {
			return "anonymous" + f.getLineno();
		} else {
			return functionName.toSource();
		}
	}
	
	
	@Override
	public boolean visit(AstNode node) {
		
		if(shouldTrackFunctionNodes){
			if(node instanceof FunctionNode){
				functionNodes.add(getFunctionName((FunctionNode)node));
			}
			
		}
		if (shouldTrackFunctionCalls){
		
			if (node instanceof FunctionCall
				&& !(((FunctionCall) node).getTarget() instanceof PropertyGet)
				&& !(node instanceof NewExpression)
				&& shouldVisitFunctionCall((FunctionCall)node)
				&& functionNodes.contains(((FunctionCall)node).getTarget().toSource())){
			
				FunctionNode callerFunc=node.getEnclosingFunction();
				if(!getFunctionName(callerFunc).equals("NoFunctionNode")){
				
					Vertex caller=new Vertex(getFunctionName(callerFunc));
					Vertex callee=new Vertex(((FunctionCall)node).getTarget().toSource());
					Edge edge=new Edge(caller,callee);
					StaticCallGraph.staticCallGraph.addEdge(edge, caller, callee);
					/*	AstNode newNode=createFunctionTrackingNode(callerFunc, (FunctionCall) node);
						appendNodeAfterFunctionCall(node, newNode);
					*/
				}

			}
    
			else
				if(node instanceof Name){
					if(node.getParent() instanceof PropertyGet 
							|| (node.getParent() instanceof FunctionCall 
							&& !((FunctionCall)node.getParent()).getTarget().toSource().equals(node.toSource()))){
						if(functionNodes.contains(node.toSource())){
							FunctionNode callerFunc=node.getEnclosingFunction();
							if(!getFunctionName(callerFunc).equals("NoFunctionNode")){
								
								Vertex caller=new Vertex(getFunctionName(callerFunc));
								Vertex callee=new Vertex(node.toSource()+"_functionAttachedToEventable");
								Edge edge=new Edge(caller,caller);
								StaticCallGraph.staticCallGraph.addEdge(edge, caller, callee);
								/*	AstNode newNode=createFunctionTypeNameTrackingNode(callerFunc, (Name) node);
    								appendNodeAfterFunctionCall(node, newNode);
								*/
							}
						}
					}
				}
    	
				else{
					if(node instanceof FunctionNode){
						if(node.getParent() instanceof FunctionCall
							&& !((FunctionCall)node.getParent()).getTarget().toSource().equals(node.toSource())
							|| !(node.getParent() instanceof FunctionCall)){
	    		
							if(functionNodes.contains(getFunctionName((FunctionNode) node))){
								FunctionNode callerFunc=node.getEnclosingFunction();
								if(!getFunctionName(callerFunc).equals("NoFunctionNode")){
									
									Vertex caller=new Vertex(getFunctionName(callerFunc));
									Vertex callee=new Vertex(getFunctionName((FunctionNode)node)+"_functionAttachedToEventable");
									Edge edge=new Edge(caller,caller);
									StaticCallGraph.staticCallGraph.addEdge(edge, caller, callee);
									/*	AstNode newNode=createFunctionTypeNameTrackingNode(callerFunc,node);
	    								appendNodeAfterFunctionCall(node, newNode);
									*/
								}
							}
						}
					}
    	
				}
		}
	
		return true;
		
	}


}
