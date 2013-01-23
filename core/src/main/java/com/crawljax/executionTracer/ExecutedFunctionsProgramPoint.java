package com.crawljax.executionTracer;

import org.json.JSONArray;

import org.json.JSONException;

import com.crawljax.core.CrawljaxException;

@Deprecated
public class ExecutedFunctionsProgramPoint extends ProgramPoint {





	public ExecutedFunctionsProgramPoint(String name, String lineNo) {
		super(name, lineNo);

		
	}


	

	@Override
	public String getTraceRecord(JSONArray data) throws CrawljaxException, JSONException {
		
		StringBuffer result = new StringBuffer();

		result.append(name + "::" + lineNo + "\n");

			for (int i = 0; i < data.length(); i++) {
				
				JSONArray item = data.getJSONArray(i);

				result.append(item.get(0) + "::" + item.get(1) );
					
				
			}
				
		result.append("\n");
		result.append("================================================");	
		result.append("\n");

		return result.toString();
	}
}
