package com.crawljax.examples;



import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.condition.UrlCondition;
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

//	private static final String URL = "http://localhost:8080//Ghostbusters/Ghostbusters.htm";
//	private static final String URL = "	http://localhost:8080/symbol/Symbol.html";
//	private static final String URL="http://localhost:8080/BunnyHunt/index.html";
//	private static final String URL= "http://localhost:8080/cartDemo/cartDemo.html";
//	private static final String URL = "http://localhost:8080//same-game/same-game.htm";
//	private static final String URL="http://localhost:8080/tunnel/tunnel.htm";
//	private static final String URL="http://localhost:8080/fractal_viewer/index.php";
//	private static final String URL="http://localhost:8080/pacman/index.html";
//	private static final String URL="http://localhost:8080/homeostasis/index.html";
//	private static final String URL="http://localhost:8080/galleria/themes/classic/classic-demo.html";
//	private static final String URL="http://127.0.0.1/phormer331/admin.php";
//	private static final String URL="http://localhost:8080/tinymce_3_3_9_2/tinymce/examples/index.html";
//	private static final String URL="http://localhost:8080/peg/peg.html";
//	private static final String URL="http://localhost:8080/aviary.com/index.html";
	private static final String URL="http://localhost:8080/narrowdesign/www.narrowdesign.com/index.html";
//	private static final String URL="http://localhost:8080/jointLondon/www.jointlondon.com/index.html";
//	private static final String URL="http://127.0.0.1/elFinder-2.x/elfinder.src.html";
//	private static final String URL="http://127.0.0.1/elfinder-2.0-rc1/elfinder.html";
	
	private static final int MAX_DEPTH = 0; // this indicates no depth-limit




	private static final int MAX_NUMBER_STATES = 100;

	private GuidedCrawljaxExampleSettings() {

	}

	private static CrawljaxConfiguration getCrawljaxConfiguration() {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setCrawlSpecification(getCrawlSpecification());
		config.setThreadConfiguration(getThreadConfiguration());
		config.setBrowser(BrowserType.firefox);
		
/*		ProxyConfiguration prox=new ProxyConfiguration();	
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
*/		return config;
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
	//	crawler.addCrawlCondition("only crawl this site", new UrlCondition("cartDemo".toLowerCase()));
				
		

		crawler.setEfficientCrawling(false);  // this is the default setting

		boolean doEfficientCrawling = false;

		if (doEfficientCrawling){
			crawler.setEfficientCrawling(true);
			crawler.setClickOnce(true);
		}
		// click these elements
		boolean phormer = false; 
		crawler.setClickOnce(false);
		if (!phormer){
		//	crawler.click("a");
		//	crawler.click("input");
			crawler.click("div");
			crawler.click("h3");
		//	crawler.click("button");
		//	crawler.click("img");
			//defining clickables
	//		crawler.click("a");
	//		crawler.click("img");
	//		crawler.click("span");
	//		crawler.click("div");
	//		crawler.click("li");
	//		crawler.click("input");
			
			
			crawler.click("polygon");
	//		crawler.click("button");
	//		crawler.click("span");
	//		crawler.dontClick("a").withAttribute("href", "account/login.html");
	//		crawler.dontClick("a").withAttribute("href", "account/signup.html");
	//		crawler.dontClick("a").withAttribute("class", "simpleCart_checkout");
	//		crawler.click("img");
	//		crawler.click("button");
			
	/*		crawler.dontClick("a").withAttribute("href", "admin.php");
			crawler.dontClick("a").withAttribute("title", "RSS Feed");
			crawler.dontClick("a").withAttribute("href", "mailto%");
			crawler.click("input").withAttribute("type", "submit");
			crawler.click("div");

			crawler.click("p").withAttribute("id", "welcome");
			crawler.addCrawlCondition("Only crawl symbol game", new UrlCondition("symbol"));
			
			
			crawler.clickDefaultElements();
	/*		
			crawler.click("input").withAttribute("type", "submit");
			crawler.click("td");
			crawler.setWaitTimeAfterEvent(2000);
			crawler.setWaitTimeAfterReloadUrl(100);
			crawler.setMaximumRuntime(120);
	*/	

//			crawler.click("p").withAttribute("id", "welcome");
//			crawler.addCrawlCondition("Only crawl symbol game", new UrlCondition("symbol"));
			crawler.setWaitTimeAfterEvent(1000);
//			crawler.setWaitTimeAfterReloadUrl(6000);
//			crawler.click("div").withAttribute("id", "bunny%");
		}else{

			// this is just for the TuduList application
			Form form=new Form();
			
			
			form.field("loginAdminPass").setValue("admin");

			//addList.field("description").setValue("test");
			InputSpecification input = new InputSpecification();
			input.setValuesInForm(form).beforeClickElement("input").withAttribute("type", "submit");
			
			crawler.setInputSpecification(input);
			crawler.click("a");
			crawler.click("img");
			crawler.click("span");
			crawler.click("div");
	//		crawler.click("li");
			crawler.click("input");
			
			
	//		crawler.click("polygon");
			crawler.click("button");
	//		crawler.click("span");
			crawler.dontClick("a").withAttribute("href", "account/login.html");
			crawler.dontClick("a").withAttribute("href", "account/signup.html");

		}


		// except these
		crawler.dontClick("a").underXPath("//DIV[@id='guser']");
		crawler.dontClick("a").withText("Language Tools");
		
		if (!phormer)
			crawler.setInputSpecification(getInputSpecification());

		
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
	//		System.setProperty("webdriver.firefox.bin" ,"/ubc/ece/home/am/grads/shabnamm/program-files/firefox18/firefox/firefox");
			CrawljaxController crawljax = new CrawljaxController(getCrawljaxConfiguration());
			crawljax.run();

			String outputdir = "jointLondon-output";

	//		writeStateFlowGraphToFile(crawljax.getSession().getStateFlowGraph(), outputdir);
	//		writeAllPossiblePathToFile(crawljax.getSession().getStateFlowGraph(), outputdir);
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
			
		
			Comparator<StateVertex> cp=new Comparator<StateVertex>() {

				@Override
				public int compare(StateVertex s1, StateVertex s2) {
			        if(Integer.parseInt(s1.getName().replace("state", "").trim())>Integer.parseInt(s2.getName().replace("state", "").trim()))
			        	return 1;
			        else if(Integer.parseInt(s1.getName().replace("state", "").trim())<Integer.parseInt(s2.getName().replace("state", "").trim())){
			        	return -1;
			        }
			        return 0;
					
				}
				
				
			};
			ArrayList<StateVertex> stateVertexList=new ArrayList<StateVertex>(keys);
			Collections.sort(stateVertexList, cp);
			iter=keys.iterator();
			
			for(int i=0;i<stateVertexList.size();i++){
				StateVertex end=stateVertexList.get(i);
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
