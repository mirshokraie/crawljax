package com.crawljax.executionTracer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.util.Helper;

public class JSFuncExecutionTracer extends ExecutionTracer 
	
	//implements PreStateCrawlingPlugin, /*OnNewStatePlugin,*/ PostCrawlingPlugin, PreCrawlingPlugin, GeneratesOutput {
		{
		
		private static final Logger LOGGER = Logger.getLogger(JSFuncExecutionTracer.class.getName());


		/**
		 * @param filename
		 *            How to name the file that will contain the assertions after execution.
		 */
		public JSFuncExecutionTracer() {
			super();
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

		@Override
		public void preStateCrawling(CrawlSession session, List<CandidateElement> candidateElements) {
	        
		

			try {

				LOGGER.info("Reading execution trace");

				LOGGER.info("Parsing JavaScript execution trace");

				
				session.getBrowser().executeJavaScript("sendReally();");
				Thread.sleep(ONE_SEC);

				FuncCallTrace trace = new FuncCallTrace();

			/*	PrintWriter file = new PrintWriter(filename);
				file.write(trace.parse(points));
				file.write('\n');
				file.close();
			*/	
				LOGGER.info("Saved execution trace in the dynamic call graph");

				points = new JSONArray();
			} catch (CrawljaxException we) {
				we.printStackTrace();
				LOGGER.error("Unable to get instrumentation log from the browser");
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		
		
		
		
		
		@Override
		public void onNewState(CrawlSession session) {
	        

			try {

				LOGGER.info("Reading execution trace");

				LOGGER.info("Parsing JavaScript execution trace");

				
				session.getBrowser().executeJavaScript("sendReally();");
				Thread.sleep(ONE_SEC);

				FuncCallTrace trace = new FuncCallTrace();

		/*		PrintWriter file = new PrintWriter(filename);
				file.write(trace.parse(points));
				file.write('\n');
				file.close();
		*/		
				LOGGER.info("Saved execution trace in the dynamic call graph");

				points = new JSONArray();
			} catch (CrawljaxException we) {
				we.printStackTrace();
				LOGGER.error("Unable to get instrumentation log from the browser");
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			
	






}
