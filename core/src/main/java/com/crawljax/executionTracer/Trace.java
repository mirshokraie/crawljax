package com.crawljax.executionTracer;

import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.core.CrawljaxException;

public abstract class Trace {




	public abstract FuncCallProgramPoint addFuncCallProgramPoint(String name, String lineNo);
	public abstract EventHandlerProgramPoint addEventHandlerProgramPoint(String name, String lineNo);
	public abstract ExecutedFunctionsProgramPoint addExecutedFuncsProgramPoint(String name, String lineNo);
	public abstract String parse(JSONArray jsonObject, int bufferActualLength) throws JSONException, CrawljaxException; 
	

	

}
