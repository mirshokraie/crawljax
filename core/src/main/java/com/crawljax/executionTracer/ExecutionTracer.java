package com.crawljax.executionTracer;




import java.util.List;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.crawljax.core.CrawlSession;

import com.crawljax.core.plugin.OnFireEventSuccessPlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateMachine;


public abstract class ExecutionTracer
				implements OnFireEventSuccessPlugin/*PreStateCrawlingPlugin, OnNewStatePlugin */{
	
	
	protected static final int ONE_SEC = 1000;

	protected static JSONArray points = new JSONArray();
	protected static int bufferActualLength;



	public ExecutionTracer() {
		

	}



	/*
	@Override
	public abstract void onNewState(CrawlSession session);
     */  


	@Override
	public abstract void onFireEventSuccessed(Eventable eventable, List<Eventable> path, CrawlSession session, StateMachine stateMachine);
	/**
	 * 
	 * @param string
	 *            The JSON-text to save.
	 */
	public static void addPoint(String string) {
		JSONArray buffer = null;
	
		try {
		
			int index=0;
			buffer = new JSONArray(string);
			for (int i = 0; i < buffer.length(); i++) {
				points.put(index,buffer.get(i));
				index++;
			
			}
			bufferActualLength=buffer.length();

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}


}
