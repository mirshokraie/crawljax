package com.crawljax.executionTracer;


import java.util.List;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateMachine;
import com.crawljax.globals.ExecutedFunctions;

@Deprecated
public class JSExecutedFuncsExecTracer extends ExecutionTracer 
	
	//implements PreStateCrawlingPlugin, /*OnNewStatePlugin,*/ PostCrawlingPlugin, PreCrawlingPlugin, GeneratesOutput {
		{
		
		private static final Logger LOGGER = LoggerFactory.getLogger(JSExecutedFuncsExecTracer.class.getName());
	
		/**
		 * @param filename
		 *            How to name the file that will contain the assertions after execution.
		 */
		public JSExecutedFuncsExecTracer() {
			super();
		}

		@Override
		public void onFireEventSuccessed(Eventable eventable,
				List<Eventable> path, CrawlSession session, StateMachine stateMachine) {
			try {
				

				LOGGER.info("Reading execution trace");

				LOGGER.info("Parsing JavaScript execution trace");

				
				session.getBrowser().executeJavaScript("sendReally();");
				Thread.sleep(ONE_SEC);

				ExecutedFunctionsTrace trace = new ExecutedFunctionsTrace();
				String input=trace.parse(points);
				String[] lines=input.split("\n");
				String functionName="";
				String scopeName="";
				for(int i=0;i<lines.length && !lines.equals("");i++){
					
					scopeName=lines[i].split("::")[0];
					i++;
					while (!lines[i].equals("================================================")){
														
						functionName=lines[i].split("::")[0];
						ExecutedFunctions.executedFuncList.add(functionName);
						i++;
							
					}
					
				}
				
					
				LOGGER.info("Saved execution trace in executedFuncList set");
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
				for(int i=0;i<lines.length && !lines.equals("");i++){
					
					scopeName=lines[i].split("::")[0];
					i++;
					while (!lines[i].equals("================================================")){
														
						functionName=lines[i].split("::")[0];
						ExecutedFunctions.executedFuncList.add(functionName);
						i++;
							
					}
					
				}
				
					
				LOGGER.info("Saved execution trace in executedFuncList set");
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
