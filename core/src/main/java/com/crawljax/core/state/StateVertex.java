package com.crawljax.core.state;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import net.jcip.annotations.GuardedBy;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.crawljax.core.CandidateCrawlAction;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CandidateElementExtractor;
import com.crawljax.core.CrawlQueueManager;
import com.crawljax.core.Crawler;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.TagElement;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.globals.GlobalVars;
import com.crawljax.randomGenerator.RandomGen;
import com.crawljax.util.Helper;
import com.google.common.collect.ImmutableList;

/**
 * The state vertex class which represents a state in the browser. This class implements the
 * Iterable interface because on a StateVertex it is possible to iterate over the possible
 * CandidateElements found in this state. When iterating over the possible candidate elements every
 * time a candidate is returned its removed from the list so it is a one time only access to the
 * candidates.
 * 
 * @author mesbah
 * @version $Id$
 */
public class StateVertex implements Serializable {
	
	//Shabnam: stores number of remaining CandidateElements
	private int numCandidateElements = 0;
	//Shabnam: used to store path to this state. Note that crawlPath stores path to the parent (source state) of this state
	private List<Eventable> crawlPathToState = new ArrayList<Eventable>();

	//Shabnam
	private List<CandidateElement> candidateElemList;
	private static final long serialVersionUID = 123400017983488L;

	private static final Logger LOGGER = LoggerFactory.getLogger(StateVertex.class);
	private long id;
	private String name;
	private String dom;
	private final String strippedDom;
	private final String url;
	private boolean guidedCrawling = true;

	/**
	 * This list is used to store the possible candidates. If it is null its not initialised if it's
	 * a empty list its empty.
	 */
	private LinkedBlockingDeque<CandidateCrawlAction> candidateActions;

	private final ConcurrentHashMap<Crawler, CandidateCrawlAction> registerdCandidateActions =
	        new ConcurrentHashMap<Crawler, CandidateCrawlAction>();
	private final ConcurrentHashMap<Crawler, CandidateCrawlAction> workInProgressCandidateActions =
	        new ConcurrentHashMap<Crawler, CandidateCrawlAction>();

	private final Object candidateActionsSearchLock = new String("");

	private final LinkedBlockingDeque<Crawler> registeredCrawlers =
	        new LinkedBlockingDeque<Crawler>();

	/**
	 * Default constructor to support saving instances of this class as an XML.
	 */
	public StateVertex() {
		this.strippedDom = "";
		this.url = "";
	}

	/**
	 * Creates a current state without an url and the stripped dom equals the dom.
	 * 
	 * @param name
	 *            the name of the state
	 * @param dom
	 *            the current DOM tree of the browser
	 */
	public StateVertex(String name, String dom) {
		this(null, name, dom, dom);
	}

	/**
	 * Defines a State.
	 * 
	 * @param url
	 *            the current url of the state
	 * @param name
	 *            the name of the state
	 * @param dom
	 *            the current DOM tree of the browser
	 * @param strippedDom
	 *            the stripped dom by the OracleComparators
	 */
	public StateVertex(String url, String name, String dom, String strippedDom) {
		this.url = url;
		this.name = name;
		this.dom = dom;
		this.strippedDom = strippedDom;
	}

	/**
	 * Retrieve the name of the StateVertex.
	 * 
	 * @return the name of the StateVertex
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieve the DOM String.
	 * 
	 * @return the dom for this state
	 */
	public String getDom() {
		return dom;
	}

	/**
	 * @return the stripped dom by the oracle comparators
	 */
	public String getStrippedDom() {
		return strippedDom;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returns a hashcode. Uses reflection to determine the fields to test.
	 * 
	 * @return the hashCode of this StateVertex
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		if (strippedDom == null || "".equals(strippedDom)) {
			builder.append(dom);
		} else {
			builder.append(strippedDom);
		}

		return builder.toHashCode();
	}

	/**
	 * Compare this vertex to a other StateVertex.
	 * 
	 * @param obj
	 *            the Object to compare this vertex
	 * @return Return true if equal. Uses reflection.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof StateVertex)) {
			return false;
		}

		if (this == obj) {
			return true;
		}
		final StateVertex rhs = (StateVertex) obj;

		return new EqualsBuilder().append(this.strippedDom, rhs.getStrippedDom())
		        .append(this.guidedCrawling, rhs.guidedCrawling).isEquals();
	}

	/**
	 * Returns the name of this state as string.
	 * 
	 * @return a string representation of the current StateVertex
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Return the size of the DOM in bytes.
	 * 
	 * @return the size of the dom
	 */
	public int getDomSize() {
		return getDom().getBytes().length;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param dom
	 *            the dom to set
	 */
	public void setDom(String dom) {
		this.dom = dom;
	}

	/**
	 * @return if this state is created through guided crawling.
	 */
	public boolean isGuidedCrawling() {
		return guidedCrawling;
	}

	/**
	 * @param guidedCrawling
	 *            true if set through guided crawling.
	 */
	public void setGuidedCrawling(boolean guidedCrawling) {
		this.guidedCrawling = guidedCrawling;
	}

	/**
	 * search for new Candidates from this state. The search for candidates is only done when no
	 * list is available yet (candidateActions == null).
	 * 
	 * @param candidateExtractor
	 *            the CandidateElementExtractor to use.
	 * @param crawlTagElements
	 *            the tag elements to examine.
	 * @param crawlExcludeTagElements
	 *            the elements to exclude.
	 * @param clickOnce
	 *            if true examine each element once.
	 * @return true if the searchForCandidateElemens has run false otherwise
	 */
	@GuardedBy("candidateActionsSearchLock")
	public boolean searchForCandidateElements(CandidateElementExtractor candidateExtractor,
	        List<TagElement> crawlTagElements, List<TagElement> crawlExcludeTagElements,
	        boolean clickOnce, StateFlowGraph sfg, boolean isEfficientCrawling) {
		synchronized (candidateActionsSearchLock) {
			if (candidateActions == null) {
				candidateActions = new LinkedBlockingDeque<CandidateCrawlAction>();
			} else {
				return false;
			}
		}
		// TODO read the eventtypes from the crawl elements instead
		List<String> eventTypes = new ArrayList<String>();
		eventTypes.add(EventType.click.toString());

		try {
			List<CandidateElement> candidateList =
			        candidateExtractor.extract(crawlTagElements, crawlExcludeTagElements,
			                clickOnce, this);
			//Shabnam 
			numCandidateElements = candidateList.size();
			candidateElemList=new ArrayList<CandidateElement>(candidateList);
			//Shabnam
			int alternateNumCandidateElements=0;
			List<CandidateElement> alternateCandidateElemList=new ArrayList<CandidateElement>();
			//Shabnam
			sfg.updateExecutedFunctions(GlobalVars.executedFuncList);
			try {
				sfg.updateStatesPotentialFuncs(this, GlobalVars.eventableElementsMap);
			} catch (SAXException e1) {
			
				e1.printStackTrace();
			} catch (IOException e1) {
		
				e1.printStackTrace();
			}
			

			if (isEfficientCrawling){
				/*Shabnam: perform sorting on candidate elements based on the number of new
				potential functions that each element may exercise*/
				int[] indices = new int[candidateList.size()];
				int[] newPotentialfuncs = new int[candidateList.size()];
				
				for (int i=0; i<candidateList.size(); i++){
					indices[i]=i;
					newPotentialfuncs[i]= sfg.getNoElementNewPotentialFuncs(this, candidateList.get(i));
				}
				
				removeElemsWithRepeatedPotentialfuncs(candidateList,newPotentialfuncs,sfg);
	/*			ArrayList<Integer> elemsWithRepeatedPotentialFuncs=new ArrayList<Integer>();
				for(int i=0;i<candidateList.size();i++){
					for(int j=i+1;j<candidateList.size();j++){
						if(newPotentialfuncs[i]==newPotentialfuncs[j]){
							Set<String> seti=sfg.getElementNewPotentialFuncs(this, candidateList.get(i));
							Set<String> setj=sfg.getElementNewPotentialFuncs(this, candidateList.get(j));
							if(seti.equals(setj)){
								elemsWithRepeatedPotentialFuncs.add(j);
//								newPotentialfuncs[j]=0;
								
							}
						}
					}
				}
				RandomGen random=new RandomGen();
				
				int thershold=(int) Math.round(elemsWithRepeatedPotentialFuncs.size()*0.5);
				for(int count=0;count<thershold;count++){
					int index=elemsWithRepeatedPotentialFuncs.get(random.getNextRandomInt(elemsWithRepeatedPotentialFuncs.size()));
					newPotentialfuncs[index]=0;
				}
			
	*/			
				int temp_idx; int temp_newPotentialfuncs;
				for (int i=0; i<candidateList.size(); i++)
					for (int j=i; j<candidateList.size(); j++)
						if (newPotentialfuncs[i] < newPotentialfuncs[j]){
							temp_idx = indices[i];  
							indices[i] = indices[j];
							indices[j] = temp_idx;
							temp_newPotentialfuncs = newPotentialfuncs[i];  
							newPotentialfuncs[i] = newPotentialfuncs[j]; 
							newPotentialfuncs[j] = temp_newPotentialfuncs;
						}
				
				ArrayList<org.w3c.dom.Element> elemList=sfg.getClickableElements(this);

				ArrayList<CandidateElement> elemListPresentInCurrDom=new ArrayList<CandidateElement>();
				ArrayList<CandidateElement> unfoundedElems=new ArrayList<CandidateElement>();
				for (int i=0; i<candidateList.size(); i++)
				{
					for (String eventType : eventTypes) { 
						//Shabnam select only clickable ones that we detect before and ignore the others
						boolean select=false;
						for(int j=0;j<elemList.size();j++){
							if(elemList.get(j).hasAttribute("id"))
								if(elemList.get(j).getAttribute("id").equals(
										candidateList.get(indices[i]).getElement().getAttribute("id"))){
									elemListPresentInCurrDom.add(candidateList.get(indices[i]));
									select=true;
									break;
								}
						}
						if(select){ //&& newPotentialfuncs[i]!=0){
							if (eventType.equals(EventType.click.toString())) {
								candidateActions.add(new CandidateCrawlAction(candidateList.get(indices[i]),
										EventType.click));
								alternateCandidateElemList.add(candidateList.get(indices[i]));
								alternateNumCandidateElements++;
								System.out.println(candidateList.get(indices[i]).getGeneralString().toString()+"*****"+"\n");
						
							
							} else {
								if (eventType.equals(EventType.hover.toString())) {
									candidateActions.add(new CandidateCrawlAction(candidateList.get(indices[i]),
											EventType.hover));
									alternateCandidateElemList.add(candidateList.get(indices[i]));
									alternateNumCandidateElements++;
								
								} else {
									LOGGER.warn("The Event Type: " + eventType + " is not supported.");
								}
							}
							

							
						}
						
						else if(!select){
							
							if (eventType.equals(EventType.click.toString())) {
								candidateActions.add(new CandidateCrawlAction(candidateList.get(indices[i]),
										EventType.click));
								unfoundedElems.add(candidateList.get(indices[i]));
								alternateNumCandidateElements++;
						
							
							} else {
								if (eventType.equals(EventType.hover.toString())) {
									candidateActions.add(new CandidateCrawlAction(candidateList.get(indices[i]),
											EventType.hover));
									unfoundedElems.add(candidateList.get(indices[i]));
									alternateNumCandidateElements++;
								
								} else {
									LOGGER.warn("The Event Type: " + eventType + " is not supported.");
								}
							}
							
						}
					}
				}
				
				for(CandidateElement unfounded:unfoundedElems){
					alternateCandidateElemList.add(unfounded);
				}
				if(candidateActions.size()==0){
					if(elemListPresentInCurrDom.size()!=0){
						RandomGen rand=new RandomGen();
						int index=rand.getNextRandomInt(elemListPresentInCurrDom.size());
						candidateActions.add(new CandidateCrawlAction(elemListPresentInCurrDom.get(index),
								EventType.click));
						alternateCandidateElemList.add(elemListPresentInCurrDom.get(index));
						alternateNumCandidateElements++;
					}
				}
				

				//Shabnam: replacing candidate elements with the ones that we detected
				
				if(alternateCandidateElemList.size()!=0){
						candidateElemList=alternateCandidateElemList;
						numCandidateElements=alternateCandidateElemList.size();
				}
				else
					numCandidateElements=candidateElemList.size();
				if(numCandidateElements==0){
					sfg.removeFromNotFullExpandedStates(this);
				}
				System.out.println(candidateElemList.size() +" new elements detected for state " + this.getName());
				
			}
			else{
				for (CandidateElement candidateElement : candidateList) {
					for (String eventType : eventTypes) {
						if (eventType.equals(EventType.click.toString())) {
							candidateActions.add(new CandidateCrawlAction(candidateElement,
									EventType.click));
						} else {
							if (eventType.equals(EventType.hover.toString())) {
								candidateActions.add(new CandidateCrawlAction(candidateElement,
										EventType.hover));
							} else {
								LOGGER.warn("The Event Type: " + eventType + " is not supported.");
							}
						}
					}
				}
			} 
		}
		catch (CrawljaxException e) {
			LOGGER.error(
			        "Catched exception while searching for candidates in state " + getName(), e);
		}
/*		if(candidateActions.size()==0){
			
			
				for(int i=0;i<candidateElemList.size();i++){
					for (String eventType : eventTypes) {
				
						if (eventType.equals(EventType.click.toString())) {
							candidateActions.add(new CandidateCrawlAction(candidateElemList.get(i),
									EventType.click));
						} 
						else {
							if (eventType.equals(EventType.hover.toString())) {
								candidateActions.add(new CandidateCrawlAction(candidateElemList.get(i),
										EventType.hover));
							} 
							else {
								LOGGER.warn("The Event Type: " + eventType + " is not supported.");
							}
						}
					}
				}
			
		}
	*/	return candidateActions.size() > 0; // Only notify of found candidates when there are...

	}

	/**
	 * Return a list of UnprocessedCandidates in a List.
	 * 
	 * @return a list of candidates which are unprocessed.
	 */
	public List<CandidateElement> getUnprocessedCandidateElements() {
		List<CandidateElement> list = new ArrayList<CandidateElement>();
		if (candidateActions == null) {
			return list;
		}
		CandidateElement last = null;
		for (CandidateCrawlAction candidateAction : candidateActions) {
			if (last != candidateAction.getCandidateElement()) {
				last = candidateAction.getCandidateElement();
				list.add(last);
			}
		}
		return list;
	}

	/**
	 * Removes Candidate Actions on candidateElements that have been removed by the pre-state crawl
	 * plugin.
	 * 
	 * @param candidateElements
	 */
	public void filterCandidateActions(List<CandidateElement> candidateElements) {
		if (candidateActions == null) {
			return;
		}
		Iterator<CandidateCrawlAction> iter = candidateActions.iterator();
		CandidateCrawlAction currentAction;
		while (iter.hasNext()) {
			currentAction = iter.next();
			if (!candidateElements.contains(currentAction.getCandidateElement())) {
				iter.remove();
				//Shabnam
				numCandidateElements--;
				LOGGER.info("filtered candidate action: " + currentAction.getEventType().name()
				        + " on " + currentAction.getCandidateElement().getGeneralString());

			}
		}
	}

	/**
	 * @return a Document instance of the dom string.
	 * @throws SAXException
	 *             if an exception is thrown.
	 * @throws IOException
	 *             if an exception is thrown.
	 */
	public Document getDocument() throws SAXException, IOException {
		return Helper.getDocument(this.dom);
	}

	/**
	 * This is the main work divider function, calling this function will first look at the
	 * registeedCandidateActions to see if the current Crawler has already registered itself at one
	 * of the jobs. Second it tries to see if the current crawler is not already processing one of
	 * the actions and return that action and last it tries to find an unregistered candidate. If
	 * all else fails it tries to return a action that is registered by an other crawler and
	 * disables that crawler.
	 * 
	 * @param requestingCrawler
	 *            the Crawler placing the request for the Action
	 * @param manager
	 *            the manager that can be used to remove a crawler from the queue.
	 * @return the action that needs to be performed by the Crawler.
	 */
	public CandidateCrawlAction pollCandidateCrawlAction(Crawler requestingCrawler,
	        CrawlQueueManager manager) {
		CandidateCrawlAction action = registerdCandidateActions.remove(requestingCrawler);
		if (action != null) {
			workInProgressCandidateActions.put(requestingCrawler, action);
			return action;
		}
		action = workInProgressCandidateActions.get(requestingCrawler);
		if (action != null) {
			return action;
		}
		action = candidateActions.pollFirst();
		if (action != null) {
			workInProgressCandidateActions.put(requestingCrawler, action);
			return action;
		} else {
			Crawler c = registeredCrawlers.pollFirst();
			if (c == null) {
				return null;
			}
			do {
				if (manager.removeWorkFromQueue(c)) {
					LOGGER.info("Crawler " + c + " REMOVED from Queue!");
					action = registerdCandidateActions.remove(c);
					if (action != null) {
						/*
						 * We got a action and removed the registeredCandidateActions for the
						 * crawler, remove the crawler from queue as the first thinng. As the
						 * crawler might just have started the run method of the crawler must also
						 * be added with a check hook.
						 */
						LOGGER.info("Stolen work from other Crawler");
						return action;
					} else {
						LOGGER.warn("Oh my! I just removed " + c
						        + " from the queue with no action!");
					}
				} else {
					LOGGER.warn("FAILED TO REMOVE " + c + " from Queue!");
				}
				c = registeredCrawlers.pollFirst();
			} while (c != null);
		}
		return null;
	}

	/**
	 * Register an assignment to the crawler.
	 * 
	 * @param newCrawler
	 *            the crawler that wants an assignment
	 * @return true if the crawler has an assignment false otherwise.
	 */
	public boolean registerCrawler(Crawler newCrawler) {
		CandidateCrawlAction action = candidateActions.pollLast();
		if (action == null) {
			return false;
		}
		registeredCrawlers.offerFirst(newCrawler);
		registerdCandidateActions.put(newCrawler, action);
		return true;
	}

	/**
	 * Register a Crawler that is going to work, tell if his must go on or abort.
	 * 
	 * @param crawler
	 *            the crawler to register
	 * @return true if the crawler is successfully registered
	 */
	public boolean startWorking(Crawler crawler) {
		CandidateCrawlAction action = registerdCandidateActions.remove(crawler);
		registeredCrawlers.remove(crawler);
		if (action == null) {
			return false;
		} else {
			workInProgressCandidateActions.put(crawler, action);
			return true;
		}
	}

	/**
	 * Notify the current StateVertex that the given crawler has finished working on the given
	 * action.
	 * 
	 * @param crawler
	 *            the crawler that is finished
	 * @param action
	 *            the action that have been examined
	 */
	public void finishedWorking(Crawler crawler, CandidateCrawlAction action) {
		candidateActions.remove(action);
		registerdCandidateActions.remove(crawler);
		workInProgressCandidateActions.remove(crawler);
		registeredCrawlers.remove(crawler);
	}
	
	//Shabnam  
	public void decreaseCandidateElements(){
		numCandidateElements--;
		//System.out.println("numCandidateElements for state " + this.getName() + " is " + numCandidateElements);
	}
	//Shabnam checks is the state is fully expanded. should always be used after 
	public boolean isFullyExpanded(){
		if (numCandidateElements==0)
			return true;
		return false;
	}
	//Shabnam
	public int getNumCandidateElements(){
		return numCandidateElements;
	}
	//Shabnam
	public List<Eventable> getCrawlPathToState() {
		return crawlPathToState;
	}
	//Shabnam 
	public void setCrawlPathToState(CrawlPath cp) {
		for (Eventable e: cp)
			this.crawlPathToState.add(e);
		
		System.out.println("+++++ crawlpath to state " + this.getName() + " is set to " + this.crawlPathToState);
	}
	//Shabnam
	public List<CandidateElement> getCandidateElemList(){
		return candidateElemList;
	}
	
	//Shabnam
	private void removeElemsWithRepeatedPotentialfuncs(List<CandidateElement> candidateList, int[] newPotentialfuncs, StateFlowGraph sfg){
		ArrayList<Integer> elemsWithRepeatedPotentialFuncs=new ArrayList<Integer>();
		for(int i=0;i<candidateList.size();i++){
			for(int j=i+1;j<candidateList.size();j++){
				if(newPotentialfuncs[i]==newPotentialfuncs[j] && newPotentialfuncs[i]!=0){
					Set<String> seti=sfg.getElementNewPotentialFuncs(this, candidateList.get(i));
					Set<String> setj=sfg.getElementNewPotentialFuncs(this, candidateList.get(j));
					if(seti.equals(setj)){
						if(!seti.contains("someFunction"))
							if(!elemsWithRepeatedPotentialFuncs.contains(j))
								elemsWithRepeatedPotentialFuncs.add(j);
//							newPotentialfuncs[j]=0;
						
					}
				}
			}
		}
		RandomGen random=new RandomGen();
		
		int thershold=(int) Math.round(elemsWithRepeatedPotentialFuncs.size()*0.8);
		for(int count=0;count<thershold;count++){
			
			int index=elemsWithRepeatedPotentialFuncs.get(random.getNextRandomInt(elemsWithRepeatedPotentialFuncs.size()));
			while(newPotentialfuncs[index]==0){
				index=elemsWithRepeatedPotentialFuncs.get(random.getNextRandomInt(elemsWithRepeatedPotentialFuncs.size()));
			}
				
			newPotentialfuncs[index]=0;
		}
	}
}
