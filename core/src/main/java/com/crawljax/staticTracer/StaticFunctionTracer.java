package com.crawljax.staticTracer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.Assignment;
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
import com.crawljax.globals.GlobalVars;
import com.crawljax.globals.LabeledFunctions;
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
	private final ArrayList<String> events = new ArrayList<String>() ;

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
		
		events.add("click");
		events.add("bind-2-click");
		events.add("on-2-click");
		events.add("onclick");
		
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
					Vertex callee;
					String calleeFuncName=((FunctionCall)node).getTarget().toSource();
					if(LabeledFunctions.labeledFunctions.get(calleeFuncName)!=null){
						callee=new Vertex(LabeledFunctions.labeledFunctions.get(calleeFuncName));
						
					}
					else{
						callee=new Vertex(((FunctionCall)node).getTarget().toSource());
					}
					Edge edge=new Edge(caller,callee);
					StaticCallGraph.staticCallGraph.addEdge(edge, caller, callee);
					/*	AstNode newNode=createFunctionTrackingNode(callerFunc, (FunctionCall) node);
						appendNodeAfterFunctionCall(node, newNode);
					*/
				}

			}
    
			
			
					// should track clickables such as .click, ... 
					if (node instanceof Name) {

					
						if(node.getParent() instanceof PropertyGet &&
								node.getParent().getParent() instanceof Assignment){
							Assignment assignment=(Assignment)node.getParent().getParent();
			
							AstNode rightSide=assignment.getRight();
							if(rightSide instanceof FunctionNode){
								if (events.indexOf(node.toSource())!=-1){
							
									FunctionNode handler=(FunctionNode)rightSide;
									String handlerName=getFunctionName(handler);
									FunctionNode caller=node.getEnclosingFunction();
									String name=getFunctionName(caller);
									String callerName;
									if(LabeledFunctions.labeledFunctions.get(name)!=null){
										callerName=LabeledFunctions.labeledFunctions.get(name);
										
									}
									else{
										callerName=name;
									}
									if(GlobalVars.potentialFutrueClickables.get(callerName)!=null){
										HashSet<String> handlerSet=GlobalVars.potentialFutrueClickables.get(callerName);
										handlerSet.add(handlerName);
										
									}
									else{
										HashSet<String> handlerSet=new HashSet<String>();
										handlerSet.add(handlerName);
										GlobalVars.potentialFutrueClickables.put(callerName, handlerSet);
									}
								}
							}
							
	
						}
						
						
						
						if (node.getParent() instanceof PropertyGet
						        && node.getParent().getParent() instanceof FunctionCall && !node.getParent().toSource().contains("function")) {

							List<AstNode> arguments = new ArrayList<AstNode>();
							arguments=((FunctionCall) node.getParent().getParent()).getArguments();

							
							if (events.indexOf(node.toSource()) != -1 || (arguments.size()>0 &&
							        events.indexOf(node.toSource() + "-" + arguments.size() + "-" + arguments.get(0).toSource()) != -1)) {
					
								FunctionNode caller=node.getEnclosingFunction();
								String name=getFunctionName(caller);
								String handlerName="";
								String callerName;
								if(LabeledFunctions.labeledFunctions.get(name)!=null){
									callerName=LabeledFunctions.labeledFunctions.get(name);
									
								}
								else{
									callerName=name;
								}
								if(arguments.size()==1){
									handlerName=arguments.get(0).toSource();		
									
								}
								else if(arguments.size()==2){
									handlerName=arguments.get(1).toSource();
									
								}
								
								if(GlobalVars.potentialFutrueClickables.get(callerName)!=null){
									HashSet<String> handlerSet=GlobalVars.potentialFutrueClickables.get(callerName);
									handlerSet.add(handlerName);
									
								}
								else{
									HashSet<String> handlerSet=new HashSet<String>();
									handlerSet.add(handlerName);
									GlobalVars.potentialFutrueClickables.put(callerName, handlerSet);
								}

							

							}
						}
					
			
					}
		
		}
		
		return true;
		

	}
}

