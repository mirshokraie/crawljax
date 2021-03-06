package com.crawljax.astmodifier;

import java.util.ArrayList;
import java.util.List;


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;

import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.StringLiteral;

import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.WhileLoop;

import com.crawljax.core.CrawljaxController;
import com.crawljax.staticTracer.StaticFunctionTracer;

public abstract class JSASTModifier implements NodeVisitor  {



		private final ArrayList<String> events = new ArrayList<String>() ;
		protected static final Logger LOGGER = LoggerFactory.getLogger(CrawljaxController.class.getName());
		/**
		 * list of functions that should be visited based on the function rank decision process.
		 * an empty list means that all functions should be visited.
		 */
		private static List<String> functionCallsNotToLog=new ArrayList<String>();
		
		private static List<String> executedFunctionNodes=new ArrayList<String>();
		/**
		 * This is used by the JavaScript node creation functions that follow.
		 */
		private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

		/**
		 * Contains the scopename of the AST we are visiting. Generally this will be the filename
		 */
		private String scopeName = null;
		
		/**
		 * whether to use the visit method of this class for tracking function calls
		 * or for logging variable/function-parameters
		 * shouldTrackFunctionCalls==true means we are tracking function calls
		 * shouldTrackFunctionCalls==false means that we are logging clicking events
		 */
		
	
		public boolean shouldTrackClickables;
	
		

		
		/**
		 * constructor without specifying functions that should be visited
		 */
		protected JSASTModifier(boolean shouldTrackClickables){
		
			this.shouldTrackClickables=shouldTrackClickables;
			
			
			events.add("click");
			events.add("bind-2-click");
			events.add("on-2-click");
			events.add("onclick");
			events.add("unbind-2-click");
			events.add("unbind");
			events.add("off-2-click");
			
			
			
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
		
		
		
		
		/**
		 * @param scopeName
		 *            the scopeName to set
		 */
		public void setScopeName(String scopeName) {
			this.scopeName = scopeName;
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

		/**
		 * Find out the function name of a certain node and return "anonymous" if it's an anonymous
		 * function.
		 * 
		 * @param f
		 *            The function node.
		 * @return The function name.
		 */
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



		private Block createBlockWithNode(AstNode node) {
			Block b = new Block();

			b.addChild(node);

			return b;
		}
	
		private AstNode makeSureBlockExistsAround(AstNode node) {
			
			AstNode parent = node.getParent();

			if (parent instanceof IfStatement) {
				/* the parent is an if and there are no braces, so we should make a new block */
				IfStatement i = (IfStatement) parent;

				/* replace the if or the then, depending on what the current node is */
				if (i.getThenPart().equals(node)) {
					i.setThenPart(createBlockWithNode(node));
				} else if (i.getElsePart()!=null){
					if (i.getElsePart().equals(node))
						i.setElsePart(createBlockWithNode(node));
				}
				
			} else if (parent instanceof WhileLoop) {
				/* the parent is a while and there are no braces, so we should make a new block */
				/* I don't think you can find this in the real world, but just to be sure */
				WhileLoop w = (WhileLoop) parent;
				if (w.getBody().equals(node))
					w.setBody(createBlockWithNode(node));
			} else if (parent instanceof ForLoop) {
				/* the parent is a for and there are no braces, so we should make a new block */
				/* I don't think you can find this in the real world, but just to be sure */
				ForLoop f = (ForLoop) parent;
				if (f.getBody().equals(node))
					f.setBody(createBlockWithNode(node));
			}

			return node.getParent();
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
		
		
		/**
		 * Actual visiting method.
		 * 
		 * @param node
		 *            The node that is currently visited.
		 * @return Whether to visit the children.
		 */
		@Override
		public boolean visit(AstNode node) {
			
		
				
				
/*				if(shouldTrackFunctionCalls){
					
					if (node instanceof FunctionCall
							&& !(((FunctionCall) node).getTarget() instanceof PropertyGet)
							&& !(node instanceof NewExpression)
							&& shouldVisitFunctionCall((FunctionCall)node)
							&& functionNodes.contains(((FunctionCall)node).getTarget().toSource())){
					
						FunctionNode callerFunc=node.getEnclosingFunction();
						if(!getFunctionName(callerFunc).equals("NoFunctionNode")){
							AstNode newNode=createFunctionTrackingNode(callerFunc, (FunctionCall) node);
							appendNodeAfterFunctionCall(node, newNode);
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
			    					AstNode newNode=createFunctionTypeNameTrackingNode(callerFunc, (Name) node);
			    					appendNodeAfterFunctionCall(node, newNode);
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
				    					AstNode newNode=createFunctionTypeNameTrackingNode(callerFunc,node);
				    					appendNodeAfterFunctionCall(node, newNode);
				    				}
				    			}
				    		}
				    	}
			    	
			    	}
				
				    				
				}
	*/			
				
				if(shouldTrackClickables){
					// should track ExecutedFunctions
					if (node instanceof FunctionNode && StaticFunctionTracer.functionNodes.contains(getFunctionName((FunctionNode) node))){
						AstNode newNode=createExecutedFunctionTrackingNode((FunctionNode)node);
						((FunctionNode)node).getBody().addChildToFront(newNode);
					}
					// should track clickables such as .click, ... */
					if (node instanceof Name) {

					
						if(node.getParent() instanceof PropertyGet &&
								node.getParent().getParent() instanceof Assignment){
							Assignment assignment=(Assignment)node.getParent().getParent();
							PropertyGet propGet=(PropertyGet) node.getParent();
							AstNode rightSide=assignment.getRight();
							if(rightSide instanceof FunctionNode){
								if (events.indexOf(node.toSource())!=-1){
									String str=node.toSource();
									String eventType="";
									if(str.contains("click"))
										eventType="click";
									else if(str.contains("unbind"))
										eventType="unbind";
									FunctionNode handler=(FunctionNode)rightSide;
									AstNode newNode=createFunctionAttachToEventNode(handler, propGet.getLeft(),eventType);
							//		appendNodeAfterClickEvent(node, newNode);
									handler.getBody().addChildrenToFront(newNode);
								}
							}
							
	
						}
						
						
						
						if (node.getParent() instanceof PropertyGet
						        && node.getParent().getParent() instanceof FunctionCall && !node.getParent().toSource().contains("function")) {

							List<AstNode> arguments = new ArrayList<AstNode>();
							arguments=((FunctionCall) node.getParent().getParent()).getArguments();

							
							if (events.indexOf(node.toSource()) != -1 || (arguments.size()>0 &&
							        events.indexOf(node.toSource() + "-" + arguments.size() + "-" + arguments.get(0).toSource()) != -1)) {
								
								String str=node.toSource();
								String eventType="";
								if(str.contains("click"))
									eventType="click";
								else if(str.contains("unbind"))
									eventType="unbind";
								PropertyGet propGet=(PropertyGet) node.getParent();
								if(arguments.size()==1){
									AstNode newNode=createFunctionAttachToEventNode(arguments.get(0), propGet.getLeft(),eventType);
									appendNodeAfterClickEvent(node, newNode);
									
								}
								else if(arguments.size()==2){
									AstNode newNode=createFunctionAttachToEventNode(arguments.get(1), propGet.getLeft(),eventType);
									appendNodeAfterClickEvent(node, newNode);
									
								}

							

							}
							
							else
								if(node.toSource().equals("addEventListener")){
									PropertyGet propGet=(PropertyGet) node.getParent();
									arguments = new ArrayList<AstNode>();
									arguments=((FunctionCall) node.getParent().getParent()).getArguments();
									String eventType="click";
									AstNode newNode=createFunctionAttachToEventNode(arguments.get(1), propGet.getLeft(),eventType);
									appendNodeAfterClickEvent(node, newNode);
									
								}
								else
									if(node.toSource().equals("attachEvent")){
										PropertyGet propGet=(PropertyGet) node.getParent();
										arguments = new ArrayList<AstNode>();
										arguments=((FunctionCall) node.getParent().getParent()).getArguments();
										String eventType="click";
										AstNode newNode=createFunctionAttachToEventNode(arguments.get(1), propGet.getLeft(),eventType);
										appendNodeAfterClickEvent(node, newNode);
										
									}
						}
					}
				}
						
			
		
		
			return true;
			
		}
		
		
		protected abstract AstNode createFunctionTypeNameTrackingNode(FunctionNode callerFunc, AstNode node);
		
		
		/**
		 * create node for tracking function calls
		 */
		
		protected abstract AstNode createFunctionTrackingNode(FunctionNode callerFunction, FunctionCall calleeFunction);
		
		protected abstract AstNode createExecutedFunctionTrackingNode(FunctionNode functionNode);
		
		/**
		 * create node for tracking functions attached to events
		 */
		
		protected abstract AstNode createFunctionAttachToEventNode(AstNode handler, AstNode element,String eventType);
		
		/**
		 * This method is called when the complete AST has been traversed.
		 * 
		 * @param node
		 *            The AST root node.
		 */
		public abstract void finish(AstRoot node);

		/**
		 * This method is called before the AST is going to be traversed.
		 */
		public abstract void start();
		
		

		
		public void appendNodeAfterFunctionCall(AstNode node, AstNode newNode){
    		AstNode parent = node;
    		
    		
    		while (parent!=null && ! (parent instanceof ReturnStatement) && ! (parent instanceof ExpressionStatement)){
    			
    			if(parent instanceof IfStatement){
        			AstNode parentToAttach=makeSureBlockExistsAround(parent);
        			parentToAttach.addChildAfter(newNode, parent);
        			return;
    			}
    			if(parent.getParent() instanceof WhileLoop){
    				WhileLoop whileLoop=(WhileLoop) parent.getParent();
    				AstNode parentToAttach=makeSureBlockExistsAround(whileLoop.getBody());
    				parentToAttach.addChildrenToFront(newNode);
        			return;
    			}
    			
    			if(parent.getParent() instanceof ForLoop){
    				ForLoop forLoop=(ForLoop) parent.getParent();
    				AstNode parentToAttach=makeSureBlockExistsAround(forLoop.getBody());
    				parentToAttach.addChildrenToFront(newNode);
        			return;
    			}
    			parent=parent.getParent();
    			
    		}
    		
    		
    		
    		if (parent instanceof ReturnStatement){
    			AstNode attachBefore=parent;
    			AstNode parentToAttach=makeSureBlockExistsAround(parent);
    			parentToAttach.addChildBefore(newNode, attachBefore);
    			
    			
    		}
    		
    		else if (parent!=null){
    			AstNode attachAfter=parent;
    			AstNode parentToAttach=makeSureBlockExistsAround(parent);
    			parentToAttach.addChildAfter(newNode, attachAfter);
    		}
		}

		public void appendNodeAfterClickEvent(AstNode node, AstNode newNode){
    		AstNode parent = node;
    		
    		
    		while (parent!=null && ! (parent instanceof ReturnStatement) && ! (parent instanceof ExpressionStatement)){
    			
    			if(parent instanceof IfStatement){
        			AstNode parentToAttach=makeSureBlockExistsAround(parent);
        			parentToAttach.addChildAfter(newNode, parent);
        			return;
    			}
    			if(parent.getParent() instanceof WhileLoop){
    				WhileLoop whileLoop=(WhileLoop) parent.getParent();
    				AstNode parentToAttach=makeSureBlockExistsAround(whileLoop.getBody());
    				parentToAttach.addChildrenToFront(newNode);
        			return;
    			}
    			
    			if(parent.getParent() instanceof ForLoop){
    				ForLoop forLoop=(ForLoop) parent.getParent();
    				AstNode parentToAttach=makeSureBlockExistsAround(forLoop.getBody());
    				parentToAttach.addChildrenToFront(newNode);
        			return;
    			}
    			parent=parent.getParent();
    			
    		}
    		
    		
    		
    		if (parent instanceof ReturnStatement){
    			AstNode attachBefore=parent;
    			AstNode parentToAttach=makeSureBlockExistsAround(parent);
    			parentToAttach.addChildBefore(newNode, attachBefore);
    			
    			
    		}
    		
    		else if (parent!=null){
    			AstNode attachAfter=parent;
    			AstNode parentToAttach=makeSureBlockExistsAround(parent);
    			parentToAttach.addChildAfter(newNode, attachAfter);
    		}
		}

}
