package com.crawljax.executionTracer;

import java.util.ArrayList;
import java.util.List;


import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateMachine;
import com.crawljax.globals.ExecutedFunctions;
import com.crawljax.globals.GlobalVars;

public class JSEventHandlerExecTracer extends ExecutionTracer {

	//implements PreStateCrawlingPlugin, /*OnNewStatePlugin,*/ PostCrawlingPlugin, PreCrawlingPlugin, GeneratesOutput {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JSExecutedFuncsExecTracer.class.getName());

	/**
	 * @param filename
	 *            How to name the file that will contain the assertions after execution.
	 */
	public JSEventHandlerExecTracer() {
		super();
	}

	@Override
	public void onFireEventSuccessed(Eventable eventable, List<Eventable> path,
			CrawlSession session, StateMachine stateMachine) {
		try {
			

			LOGGER.info("Reading execution trace");

			LOGGER.info("Parsing JavaScript execution trace");

			
			session.getBrowser().executeJavaScript("sendReally();");
			Thread.sleep(ONE_SEC);

			
			EventHandlerTrace trace = new EventHandlerTrace();
			String input=trace.parse(points);
			String[] lines=input.split("\n");
			String functionName="";
			String scopeName="";
			String uniqueId="";
			String handlerFunc="";
			String eventType="";
			for(int i=0;i<lines.length-1 && !lines.equals("");i++){
				
				scopeName=lines[i].split("::")[0];
				i++;
				while (!lines[i].equals("================================================")){
									
					if(lines[i].split("::")[0].equals("Eventable")){
						String[] uniqueIds=lines[i].split("::");
						handlerFunc=uniqueIds[uniqueIds.length-2];
						handlerFunc=handlerFunc.replace("'", "");
						eventType=uniqueIds[uniqueIds.length-1];
						for(int j=1;j<uniqueIds.length-2;j++){
							ArrayList<Object> elementInfo=new ArrayList<Object>();
							elementInfo.add(uniqueIds[j]);
							elementInfo.add(stateMachine.getCurrentState().toString());
							elementInfo.add(eventable);
							elementInfo.add(eventType);
							if(GlobalVars.eventableElementsMap.get(handlerFunc)!=null){
							
								GlobalVars.eventableElementsMap.get(handlerFunc).add(elementInfo);
				
							}
							else{
								ArrayList<ArrayList<Object>> newList=new ArrayList<ArrayList<Object>>();
								newList.add(elementInfo);
								GlobalVars.eventableElementsMap.put(handlerFunc,newList);
							}
						}
					}
					else 
						if(lines[i].split("::")[0].equals("FunctionExecuted")){
							functionName=lines[i].split("::")[1];
							ExecutedFunctions.executedFuncList.add(functionName);
						}
					
					i++;
						
				}
				
			}
			
				
			LOGGER.info("Saved execution trace in eventableElementsMap treeMap");
			points = new JSONArray();
		} catch (CrawljaxException we) {
			we.printStackTrace();
			LOGGER.error("Unable to get instrumentation log from the browser");
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		
	}



	/**
	 * Retrieves the JavaScript instrumentation array from the webbrowser and writes its contents in
	 *  to a file.
	 * 
	 * @param session
	 *            The crawling session.
	 * @param candidateElements
	 *            The candidate clickable elements.
	 */

	
	
	
	
	
	/*
	
	@Override
	public void onNewState(CrawlSession session) {
        

		try {
	

			LOGGER.info("Reading execution trace");

			LOGGER.info("Parsing JavaScript execution trace");

			
			session.getBrowser().executeJavaScript("sendReally();");
			Thread.sleep(ONE_SEC);

			FuncCallTrace trace = new FuncCallTrace();
			String input=trace.parse(points);
			String[] lines=input.split("\n");
			String functionName="";
			String scopeName="";
			String xpath="";
			String handlerFunc="";
			for(int i=0;i<lines.length && !lines.equals("");i++){
				
				scopeName=lines[i].split("::")[0];
				i++;
				while (!lines[i].equals("================================================")){
													
					xpath=lines[i].split("::")[0];
					handlerFunc=lines[i].split("::")[1];
					Eventables.eventableElementsMap.put(xpath, handlerFunc);
					i++;
						
				}
				
			}
			
				
			LOGGER.info("Saved execution trace in eventableElementsMap treeMap");
			points = new JSONArray();
		} catch (CrawljaxException we) {
			we.printStackTrace();
			LOGGER.error("Unable to get instrumentation log from the browser");
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
}
