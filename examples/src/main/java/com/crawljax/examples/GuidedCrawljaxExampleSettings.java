package com.crawljax.examples;

import org.apache.commons.configuration.ConfigurationException;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.configuration.ThreadConfiguration;
import com.crawljax.plugins.webscarabwrapper.WebScarabWrapper;
import com.crawljax.staticTracer.StaticFunctionTracer;
import com.crawljax.astmodifier.*;
import com.crawljax.executionTracer.*;
import com.crawljax.core.configuration.Form;



/**
 * Simple Example.

 */
public final class GuidedCrawljaxExampleSettings {


	//private static final String URL = "http://localhost:8080/tudu-dwr/";
	
	private static final String URL = "http://localhost:8080/same-game/same-game.html";


	

	private static final int MAX_DEPTH = 2; // this indicates no depth-limit
	

	private static final int MAX_NUMBER_STATES = 20;

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
		StaticFunctionTracer staticFunctionTracer=new StaticFunctionTracer(true, true);
		JSModifyProxyPlugin proxyPlugin = new JSModifyProxyPlugin(eventHandlerInstrumenter,staticFunctionTracer);
		proxyPlugin.excludeDefaults();
		
		web.addPlugin(proxyPlugin);
		JSEventHandlerExecTracer tracer = new JSEventHandlerExecTracer();
		config.addPlugin(web);
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

		// crawler.setDiverseCrawling(true);   // do guided crawling
		// crawler.setClickOnce(false);       // false: multiple click, true: click only once on each clickable

		boolean doEfficientCrawling = true;

		if (doEfficientCrawling){
			crawler.setEfficientCrawling(true);
			crawler.setClickOnce(false);
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
	*/		crawler.click("td");
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

		crawler.setClickOnce(true);
		crawler.setDepth(2);
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
		} catch (CrawljaxException e) {
			e.printStackTrace();
			System.exit(1);
		} 

	}

}
