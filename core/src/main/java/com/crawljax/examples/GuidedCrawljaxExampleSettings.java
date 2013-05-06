package com.crawljax.examples;



import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.configuration.ThreadConfiguration;
import com.crawljax.plugins.webscarabwrapper.WebScarabWrapper;
import com.crawljax.staticTracer.StaticFunctionTracer;
import com.crawljax.staticTracer.StaticLabeledFunctionTracer;
import com.crawljax.util.Helper;
import com.crawljax.util.XPathHelper;
import com.crawljax.astmodifier.*;
import com.crawljax.executionTracer.*;
import com.crawljax.globals.GlobalVars;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.state.Attribute;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;



/**
 * Simple Example.

 */
public final class GuidedCrawljaxExampleSettings {


	//private static final String URL = "http://localhost:8080/tudu-dwr/";

//	private static final String URL = "http://localhost:8080//Ghostbusters/Ghostbusters.html";
	private static final String URL = "	http://localhost:8080/symbol/Symbol.html";
//	private static final String URL = "http://localhost:8080//same-game/same-game.html";

	

	private static final int MAX_DEPTH = 0; // this indicates no depth-limit
	

	private static final int MAX_NUMBER_STATES = 0;

	private GuidedCrawljaxExampleSettings() {

	}

	private static CrawljaxConfiguration getCrawljaxConfiguration() {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setCrawlSpecification(getCrawlSpecification());
		config.setThreadConfiguration(getThreadConfiguration());
		config.setBrowser(BrowserType.firefox);
		
		ProxyConfiguration prox=new ProxyConfiguration();	
		WebScarabWrapper web = new WebScarabWrapper();
		AstEventHandlerInstrumenter eventHandlerInstrumenter=new AstEventHandlerInstrumenter();
		StaticFunctionTracer staticFunctionTracer=new StaticFunctionTracer();
		StaticLabeledFunctionTracer staticLabeledFunctionTracer=new StaticLabeledFunctionTracer();
		JSModifyProxyPlugin proxyPlugin = new JSModifyProxyPlugin(eventHandlerInstrumenter,staticFunctionTracer,staticLabeledFunctionTracer);
		proxyPlugin.excludeDefaults();
		
		web.addPlugin(proxyPlugin);
		JSEventHandlerExecTracer tracer = new JSEventHandlerExecTracer();
		config.addPlugin(tracer);
		config.addPlugin(web);
		config.setProxyConfiguration(prox);
		return config;
	}

	private static ThreadConfiguration getThreadConfiguration() {
		ThreadConfiguration tc = new ThreadConfiguration();
		tc.setBrowserBooting(true);
		tc.setNumberBrowsers(1);
		tc.setNumberThreads(1);
		return tc;
	}

	private static CrawlSpecification getCrawlSpecification() {
		CrawlSpecification crawler = new CrawlSpecification(URL);

		// crawler.setMaximumRuntime(300); 		
		

		crawler.setEfficientCrawling(true);  // this is the default setting

		boolean doEfficientCrawling = true;

		if (doEfficientCrawling){
			crawler.setEfficientCrawling(true);
			crawler.setClickOnce(true);
		}

		// click these elements
		boolean tudu = false; 

		if (!tudu){
			//defining clickables
	
	/*		crawler.click("a");
			crawler.click("div");
			crawler.click("span");
			crawler.click("img");
			crawler.click("input").withAttribute("type", "submit");
			crawler.click("div");
			crawler.click("td");
			crawler.click("p").withAttribute("id", "welcome");
	*/		crawler.click("button");
	crawler.click("button");
		}else{
			// this is just for the TuduList application
			Form form=new Form();
			Form addList=new Form();
			form.field("j_username").setValue("shabnam");
			form.field("j_password").setValue("shabnam");
			form.field("dueDate").setValue("10/10/2010");
			form.field("priority").setValue("10");
			//addList.field("description").setValue("test");
			InputSpecification input = new InputSpecification();
			input.setValuesInForm(form).beforeClickElement("input").withAttribute("type", "submit");
			input.setValuesInForm(addList).beforeClickElement("a").withAttribute("href", "javascript:addTodo();");
			crawler.setInputSpecification(input);
			crawler.click("a");
			crawler.click("img").withAttribute("id", "add_trigger_calendar");
			crawler.click("img").withAttribute("id", "edit_trigger_calendar");
			
			//crawler.click("a");
			crawler.click("div");
			crawler.click("span");
			crawler.click("img");
			//crawler.click("input").withAttribute("type", "submit");
			crawler.click("td");

			crawler.dontClick("a").withAttribute("title", "My info");
			crawler.dontClick("a").withAttribute("title", "Log out");
			crawler.dontClick("a").withAttribute("text", "Cancel");
		}


		// except these
		crawler.dontClick("a").underXPath("//DIV[@id='guser']");
		crawler.dontClick("a").withText("Language Tools");
		
		if (!tudu)
			crawler.setInputSpecification(getInputSpecification());

	//	crawler.setClickOnce(true);
	//	crawler.setDepth(2);
		// limit the crawling scope
		crawler.setMaximumStates(MAX_NUMBER_STATES);
		crawler.setDepth(MAX_DEPTH);


		return crawler;
	}

	private static InputSpecification getInputSpecification() {
		InputSpecification input = new InputSpecification();
		input.field("q").setValue("Crawljax");
		return input;
	}

	/**
	 * @param args
	 *            the command line args
	 */
	public static void main(String[] args) {
		try {
			System.setProperty("webdriver.firefox.bin" ,"/ubc/ece/home/am/grads/shabnamm/program-files/firefox18/firefox/firefox");
			CrawljaxController crawljax = new CrawljaxController(getCrawljaxConfiguration());
			crawljax.run();
			String outputdir = "same-output";
			writeStateFlowGraphToFile(crawljax.getSession().getStateFlowGraph(), outputdir);
			writeAllPossiblePathToFile(crawljax.getSession().getStateFlowGraph(), outputdir);
			writeAllPathToFile(crawljax.getSession().getStateFlowGraph(), outputdir);
		} catch (CrawljaxException e) {
			e.printStackTrace();
			System.exit(1);
		} 

	}
	
	@Deprecated
	private static void writeStateFlowGraphToFile(StateFlowGraph stateFlowGraph, String outputDir){
		try{
			
			
			StringBuffer result=new StringBuffer();
			Helper.directoryCheck(Helper.addFolderSlashIfNeeded(outputDir));
			String filename =  Helper.addFolderSlashIfNeeded(outputDir) + "stateFlowGraph" + ".txt";	
			PrintWriter file = new PrintWriter(filename);
			
			Set<StateVertex> stateVertexList=stateFlowGraph.getAllStates();
			Iterator<StateVertex> it=stateVertexList.iterator();
			while(it.hasNext()){
				StateVertex stateVertex=it.next();
				result.append(stateVertex.getName() + "\n");
				List<CandidateElement> candidateElems=stateVertex.getCandidateElemList();
				for(CandidateElement elem:candidateElems){
					Element element=elem.getElement();
					for(int i=0;i<element.getAttributes().getLength();i++){
						String attrName=element.getAttributes().item(i).getNodeName();
						String attrValue=element.getAttributes().item(i).getNodeValue();
						result.append(attrName + "::" + attrValue + "\n");
					}
					String xpath=XPathHelper.getXPathExpression(element);
					result.append("xpath::" + xpath + "\n");
					result.append("---------------------------------------------------------------------------\n");
				
				}
				result.append("===========================================================================\n");
			}
			
			file.write(result.toString());
			file.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	
	
	private static void writeAllPathToFile(StateFlowGraph stateFlowGraph, String outputDir){
		try{
			StringBuffer result=new StringBuffer();
			Helper.directoryCheck(Helper.addFolderSlashIfNeeded(outputDir));
			String filename =  Helper.addFolderSlashIfNeeded(outputDir) + "allPossiblePath" + ".txt";	
			PrintWriter file = new PrintWriter(filename);
			
			Iterator<StateVertex> it=stateFlowGraph.getAllStates().iterator();
			StateVertex index = null;
			while(it.hasNext()){
				StateVertex state=it.next();
				if(state.getName().equals("index")){
					index=state;
					break;
				}
			}
			
			List<StateVertex> leafNodeList=stateFlowGraph.getDeepStates(index);
			Set<StateVertex> keys=GlobalVars.stateCrawlPathMap.keySet();
			Iterator<StateVertex> iter=keys.iterator();
			while(iter.hasNext()){
				StateVertex state=iter.next();
				if(!leafNodeList.contains(state)){
					GlobalVars.stateCrawlPathMap.removeAll(state);
					iter=keys.iterator();
				}
			}
			
			keys=GlobalVars.stateCrawlPathMap.keySet();
			iter=keys.iterator();
			while(iter.hasNext()){
				StateVertex end=iter.next();
				String startVertexName=index.getName();
				String endVertexName=end.getName();
				
				List<List<Eventable>> events=GlobalVars.stateCrawlPathMap.get(end);
				for(List<Eventable> eventableList:events){
					result.append(startVertexName + "::" + endVertexName + "\n");
					for(Eventable event:eventableList){
						String tagName=event.getElement().getNode().getNodeName();
						result.append("tagName::" + tagName + "\n");
						NamedNodeMap attrs=event.getElement().getNode().getAttributes();
						for(int k=0;k<attrs.getLength();k++){
							String attrName=attrs.item(k).getNodeName();
							String attrValue=attrs.item(k).getNodeValue();
							result.append(attrName + "::" + attrValue + "\n");
						}
						String xpath=XPathHelper.getXPathExpression(event.getElement().getNode());
						result.append("xpath::" + xpath + "\n");
						result.append("---------------------------------------------------------------------------\n");
					}
					
					result.append("===========================================================================\n");
					
				
						
				}
			}
			file.write(result.toString());
			file.close();
			
			
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		
	}
	
	@Deprecated
	private static void writeAllPossiblePathToFile(StateFlowGraph stateFlowGraph, String outputDir){
		try{
			StringBuffer result=new StringBuffer();
			Helper.directoryCheck(Helper.addFolderSlashIfNeeded(outputDir));
			String filename =  Helper.addFolderSlashIfNeeded(outputDir) + "allPossiblePath" + ".txt";	
			PrintWriter file = new PrintWriter(filename);
			Iterator<StateVertex> it=stateFlowGraph.getAllStates().iterator();
			StateVertex index = null;
			while(it.hasNext()){
				StateVertex state=it.next();
				if(state.getName().equals("index")){
					index=state;
					break;
				}
			}
			List<List<GraphPath<StateVertex, Eventable>>> allPath=stateFlowGraph.getAllPossiblePaths(index);
			for(int i=0;i<allPath.size();i++){
				List<GraphPath<StateVertex, Eventable>> path=allPath.get(i);
				for(int j=0;j<path.size();j++){
					StateVertex start=path.get(j).getStartVertex();
					String startVertexName=start.getName();
					StateVertex end=path.get(j).getEndVertex();
					String endVertexName=end.getName();
					result.append(startVertexName + "::" + endVertexName + "\n");
					List<Eventable> events=path.get(j).getEdgeList();
					for(Eventable event:events){
						String tagName=event.getElement().getNode().getNodeName();
						result.append("tagName::" + tagName + "\n");
						NamedNodeMap attrs=event.getElement().getNode().getAttributes();
						for(int k=0;k<attrs.getLength();k++){
							String attrName=attrs.item(k).getNodeName();
							String attrValue=attrs.item(k).getNodeValue();
							result.append(attrName + "::" + attrValue + "\n");
						}
						String xpath=XPathHelper.getXPathExpression(event.getElement().getNode());
						result.append("xpath::" + xpath + "\n");
						result.append("---------------------------------------------------------------------------\n");
					}
					result.append("===========================================================================\n");
					
				}
			}
			file.write(result.toString());
			file.close();
			
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		
	}
	
	
}
