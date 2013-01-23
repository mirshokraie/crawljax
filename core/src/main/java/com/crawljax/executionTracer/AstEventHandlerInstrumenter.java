package com.crawljax.executionTracer;

import java.io.File;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;

import com.crawljax.astmodifier.JSASTModifier;

import com.crawljax.util.Helper;

public class AstEventHandlerInstrumenter extends JSASTModifier {

	public static final String JSINSTRUMENTLOGNAME = "window.jsEventHanlderExecTrace";




	/**
	 * Construct without patterns.
	 */
	public AstEventHandlerInstrumenter() {
		super(true);
	
	}
	
	@Override
	public void finish(AstRoot node) {
		/* add initialization code for the function and logging array */
		node.addChildToFront(jsLoggingFunctions());
	}

	@Override
	public void start() {
		/* nothing to do here */
	}

	
	
	@Override
	protected AstNode createFunctionTrackingNode(FunctionNode callerFunction, FunctionCall calleeFunction) {
		
		return null;
	
	}
	
	@Override
	public AstNode createFunctionTypeNameTrackingNode(FunctionNode callerFunction, AstNode calleeFunction) {
		
		return null;
	
	}
	
	
	private AstNode jsLoggingFunctions() {
		String code;

		File js = new File(this.getClass().getResource("/giveUniqueId.js").getFile());
		code = Helper.getContent(js);
		return parse(code);
	}






	@Override
	protected AstNode createFunctionAttachToEventNode(AstNode handler, AstNode element) {
		String eventHandler=handler.toSource();
		String targetElement=element.toSource();
		String enclosingFunc=getFunctionName(element.getEnclosingFunction());
		eventHandler = Helper.removeNewLines(eventHandler);
		targetElement = Helper.removeNewLines(targetElement);
		/* escape quotes */
		eventHandler = eventHandler.replaceAll("\\\"", "\\\\\"");
		eventHandler = eventHandler.replaceAll("\\\'", "\\\\\'");
		targetElement = targetElement.replaceAll("\\\"", "\\\\\"");
		targetElement = targetElement.replaceAll("\\\'", "\\\\\'");
		int lineNo=element.getLineno();
		String code=
			"send(new Array('" + getScopeName() + "::" + enclosingFunc + "', '" + lineNo +  
            "', new Array(";
		
		code += "giveUniqueId('" + targetElement + "'" + ", " + "'" + 
		eventHandler + "'"+"))));";
	//	System.out.println(code);
		return parse(code);
	}

	@Override
	protected AstNode createExecutedFunctionTrackingNode(
			FunctionNode functionNode) {
		String functionName=getFunctionName(functionNode);
	
		
		functionName = Helper.removeNewLines(functionName);
		/* escape quotes */
		functionName = functionName.replaceAll("\\\"", "\\\\\"");
		functionName = functionName.replaceAll("\\\'", "\\\\\'");
		int lineNo=functionNode.getLineno();
		String code=
			"send(new Array('" + getScopeName() + "::" + functionName + "', '" + lineNo +  
            "', new Array(";
		
		code += "addFunctionNodeTrack('" + functionName + "'" + ", " + "'" + 
		"NoMoreInfoYet" + "'"+"))));";
	//	System.out.println(code);
		return parse(code);
	
	}
	

}
