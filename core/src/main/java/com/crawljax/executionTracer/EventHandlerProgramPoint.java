package com.crawljax.executionTracer;

import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.core.CrawljaxException;

public class EventHandlerProgramPoint extends ProgramPoint {
	





	public EventHandlerProgramPoint(String name, String lineNo) {
		super(name, lineNo);

		
	}


	

	@Override
	public String getTraceRecord(JSONArray data) throws CrawljaxException, JSONException {
		
		StringBuffer result = new StringBuffer();

		result.append(name + "::" + lineNo + "\n");

			for (int i = 0; i < data.length(); i++) {
				
				JSONArray item = data.getJSONArray(i);
				if(item.get(0).equals("giveUniqueId")){
					result.append("Eventable" + "::");
					if(item.get(1) instanceof JSONArray){
						JSONArray array=(JSONArray) item.get(1);
						for(int j=0;j<array.length();j++){
							result.append(array.get(j)+ "::");
						}
						result.append(item.get(2)+"::" + item.get(3));
					}
					
					else{
						result.append(item.get(1) + "::" + item.get(2) + "::" + item.get(3) );
					}
				}
				else
					if(item.get(0).equals("addFunctionNodeTrack")){
						result.append("FunctionExecuted" + "::");								
						item = data.getJSONArray(i);
						result.append(item.get(1) + "::" + item.get(2) );	
						
					}
				result.append("\n");
				result.append("================================================");	
				result.append("\n");
					
				
			}
				

		return result.toString();
	}

}
