package com.crawljax.executionTracer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.core.CrawljaxException;

public class ExecutedFunctionsTrace extends Trace {
	
	private ArrayList<ExecutedFunctionsProgramPoint> programPoints;

	
	public ExecutedFunctionsTrace() {
		programPoints = new ArrayList<ExecutedFunctionsProgramPoint>();
	}


	@Override
	public ExecutedFunctionsProgramPoint addExecutedFuncsProgramPoint(String name, String lineNo) {
	
		ExecutedFunctionsProgramPoint p = new ExecutedFunctionsProgramPoint(name,lineNo);
		programPoints.add(p);
		return p;
	}

	@Override
	public EventHandlerProgramPoint addEventHandlerProgramPoint(String name, String lineNo){
		return null;
	}
	@Override
	public String parse(JSONArray jsonObject) throws JSONException, CrawljaxException {
	
		StringBuffer result = new StringBuffer();
		for (int j = 0; j < jsonObject.length(); j++) {
			
			JSONArray value = jsonObject.getJSONArray(j);
			String programPointName = value.getString(0);
			String lineNo = value.getString(1);
			ExecutedFunctionsProgramPoint prog = addExecutedFuncsProgramPoint(programPointName,lineNo);
			
			/* output all the variable values */
			result.append(prog.getTraceRecord(value.getJSONArray(2)));
		
		}

		return result.toString();
	}


	@Override
	public FuncCallProgramPoint addFuncCallProgramPoint(String name,
			String lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

}
