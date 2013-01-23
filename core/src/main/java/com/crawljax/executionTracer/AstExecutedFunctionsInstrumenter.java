
package com.crawljax.executionTracer;

import java.io.File;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;

import com.crawljax.astmodifier.JSASTModifier;
import com.crawljax.util.Helper;
@Deprecated
public class AstExecutedFunctionsInstrumenter extends JSASTModifier{

	public static final String JSINSTRUMENTLOGNAME = "window.jsExecFuncsTrace";




	/**
	 * Construct without patterns.
	 */
	public AstExecutedFunctionsInstrumenter() {
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

		File js = new File(this.getClass().getResource("/addFunctionNodeTrack.js").getFile());
		code = Helper.getContent(js);
		return parse(code);
	}






	@Override
	protected AstNode createFunctionAttachToEventNode(AstNode handler,
			AstNode element) {
		// TODO Auto-generated method stub
		return null;
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
