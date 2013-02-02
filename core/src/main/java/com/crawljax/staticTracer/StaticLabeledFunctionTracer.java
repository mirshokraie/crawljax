package com.crawljax.staticTracer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.LabeledStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawljaxController;
import com.crawljax.globals.GlobalVars;

			
public class StaticLabeledFunctionTracer implements NodeVisitor {

	
	

	protected static final Logger LOGGER = LoggerFactory.getLogger(CrawljaxController.class.getName());
	 /**
	  *  This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;
	
	public StaticLabeledFunctionTracer(){
		
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
		if(node instanceof LabeledStatement){
			if(((LabeledStatement)node).getStatement() instanceof FunctionNode
					&& ((LabeledStatement)node).getLabels().size()==1){
				
				FunctionNode funcNode=(FunctionNode) ((LabeledStatement)node).getStatement();
				String funcName=getFunctionName(funcNode);
				String labelName=((LabeledStatement)node).getLabels().get(0).getName();
				GlobalVars.labeledFunctions.put(labelName, funcName);
			
				
			}
	
		}
		return true;
	}
	}
