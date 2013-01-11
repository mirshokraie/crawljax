package com.crawljax.executionTracer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.core.CrawljaxException;

public abstract class Trace {




	public abstract FuncCallProgramPoint addFuncCallProgramPoint(String name, String lineNo);
	public abstract EventHandlerProgramPoint addEventHandlerProgramPoint(String name, String lineNo);

	public abstract String parse(JSONArray jsonObject) throws JSONException, CrawljaxException; 
	

	

}
