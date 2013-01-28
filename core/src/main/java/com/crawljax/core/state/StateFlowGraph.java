package com.crawljax.core.state;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.GuardedBy;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DirectedMultigraph;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.PropertyGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.crawljax.core.CandidateElement;
import com.crawljax.globals.StaticCallGraph;
import com.crawljax.graph.Edge;
import com.crawljax.graph.Vertex;


/**
 * The State-Flow Graph is a multi-edge directed graph with states (StateVetex) on the vertices and
 * clickables (Eventable) on the edges.
 */
public class StateFlowGraph implements Serializable {
	
	//Shabnam
	private ArrayList<StateVertex> notFullExpandedStates = new ArrayList<StateVertex>();
	//Shabnam
	private boolean efficientCrawling = true;
	//Shabnam map<stateVertex,[[element1,function1],[element2, function2],...]>
	private TreeMap<String,ArrayList<ArrayList<Object>>> statesPotentialFuncs = new TreeMap<String,ArrayList<ArrayList<Object>>>();

	//Shabnam map<statevertex,[function1,function2,...]>
	private TreeMap<String,HashSet<String>> statesNewPotentialFuncs = new TreeMap<String,HashSet<String>>();

	//Shabnam
	private Set<String> executedFunctions = new HashSet<String>();
	
	private static final long serialVersionUID = 923403417983488L;

	private static final Logger LOGGER = LoggerFactory.getLogger(StateFlowGraph.class.getName());

	private final DirectedGraph<StateVertex, Eventable> sfg;

	/**
	 * Intermediate counter for the number of states, not relaying on getAllStates.size() because of
	 * Thread-safety.
	 */
	private final AtomicInteger stateCounter = new AtomicInteger(1);

	/**
	 * Empty constructor.
	 */
	public StateFlowGraph() {
		sfg = new DirectedMultigraph<StateVertex, Eventable>(Eventable.class);
	}

	/**
	 * The constructor.
	 * 
	 * @param initialState
	 *            the state to start from.
	 */
	public StateFlowGraph(StateVertex initialState) {
		this();
		sfg.addVertex(initialState);
		
		
		//Shabnam
		notFullExpandedStates.add(initialState);
	}

	/**
	 * Adds a state (as a vertix) to the State-Flow Graph if not already present. More formally,
	 * adds the specified vertex, v, to this graph if this graph contains no vertex u such that
	 * u.equals(v). If this graph already contains such vertex, the call leaves this graph unchanged
	 * and returns false. In combination with the restriction on constructors, this ensures that
	 * graphs never contain duplicate vertices. Throws java.lang.NullPointerException - if the
	 * specified vertex is null. This method automatically updates the state name to reflect the
	 * internal state counter.
	 * 
	 * @param stateVertix
	 *            the state to be added.
	 * @return the clone if one is detected null otherwise.
	 * @see org.jgrapht.Graph#addVertex(Object)
	 */
	public StateVertex addState(StateVertex stateVertix) {
		return addState(stateVertix, true);
	}

	/**
	 * Adds a state (as a vertix) to the State-Flow Graph if not already present. More formally,
	 * adds the specified vertex, v, to this graph if this graph contains no vertex u such that
	 * u.equals(v). If this graph already contains such vertex, the call leaves this graph unchanged
	 * and returns false. In combination with the restriction on constructors, this ensures that
	 * graphs never contain duplicate vertices. Throws java.lang.NullPointerException - if the
	 * specified vertex is null.
	 * 
	 * @param stateVertix
	 *            the state to be added.
	 * @param correctName
	 *            if true the name of the state will be corrected according to the internal state
	 *            counter.
	 * @return the clone if one is detected null otherwise.
	 * @see org.jgrapht.Graph#addVertex(Object)
	 */
	@GuardedBy("sfg")
	public StateVertex addState(StateVertex stateVertix, boolean correctName) {
		synchronized (sfg) {
			if (!sfg.addVertex(stateVertix)) {
				// Graph already contained the vertix
				return this.getStateInGraph(stateVertix);
			} else {
				/**
				 * A new State has been added so check to see if the name is correct, remember this
				 * is the only place states can be added and we are now locked so getAllStates.size
				 * works correctly.
				 */
				if (correctName) {
					// the -1 is for the "index" state.
					int totalNumberOfStates = this.getAllStates().size() - 1;
					String correctedName =
					        makeStateName(totalNumberOfStates, stateVertix.isGuidedCrawling());
					if (!stateVertix.getName().equals("index")
					        && !stateVertix.getName().equals(correctedName)) {
						LOGGER.info("Correcting state name from  " + stateVertix.getName()
						        + " to " + correctedName);
						stateVertix.setName(correctedName);
					}
				}
			}
			stateCounter.set(this.getAllStates().size() - 1);
			// Shabnam: Add the new state to the list of unexpanded states
			notFullExpandedStates.add(stateVertix);
			LOGGER.info("State " + stateVertix + " added to the notFullExpandedStates list!");
			//Shabnam
	/*		if(efficientCrawling){
				updateStatesPotentialFuncsWithKnownElems(stateVertix);
			}
	*/	}
		return null;
	}

	/**
	 * Adds the specified edge to this graph, going from the source vertex to the target vertex.
	 * More formally, adds the specified edge, e, to this graph if this graph contains no edge e2
	 * such that e2.equals(e). If this graph already contains such an edge, the call leaves this
	 * graph unchanged and returns false. Some graphs do not allow edge-multiplicity. In such cases,
	 * if the graph already contains an edge from the specified source to the specified target, than
	 * this method does not change the graph and returns false. If the edge was added to the graph,
	 * returns true. The source and target vertices must already be contained in this graph. If they
	 * are not found in graph IllegalArgumentException is thrown.
	 * 
	 * @param sourceVert
	 *            source vertex of the edge.
	 * @param targetVert
	 *            target vertex of the edge.
	 * @param clickable
	 *            the clickable edge to be added to this graph.
	 * @return true if this graph did not already contain the specified edge.
	 * @see org.jgrapht.Graph#addEdge(Object, Object, Object)
	 */
	@GuardedBy("sfg")
	public boolean addEdge(StateVertex sourceVert, StateVertex targetVert, Eventable clickable) {
		synchronized (sfg) {
			// TODO Ali; Why is this code (if-stmt) here? Its the same as what happens in sfg.addEge
			// imo (21-01-10 Stefan).
			if (sfg.containsEdge(sourceVert, targetVert)
			        && sfg.getAllEdges(sourceVert, targetVert).contains(clickable)) {
				return false;
			}

			return sfg.addEdge(sourceVert, targetVert, clickable);
		}
	}

	/**
	 * @return the string representation of the graph.
	 * @see org.jgrapht.DirectedGraph#toString()
	 */
	@Override
	public String toString() {
		return sfg.toString();
	}

	/**
	 * Returns a set of all clickables outgoing from the specified vertex.
	 * 
	 * @param stateVertix
	 *            the state vertix.
	 * @return a set of the outgoing edges (clickables) of the stateVertix.
	 * @see org.jgrapht.DirectedGraph#outgoingEdgesOf(Object)
	 */
	public Set<Eventable> getOutgoingClickables(StateVertex stateVertix) {
		return sfg.outgoingEdgesOf(stateVertix);
	}

	/**
	 * Returns a set of all edges incoming into the specified vertex.
	 * 
	 * @param stateVertix
	 *            the state vertix.
	 * @return a set of the incoming edges (clickables) of the stateVertix.
	 * @see org.jgrapht.DirectedGraph#incomingEdgesOf(Object)
	 */
	public Set<Eventable> getIncomingClickable(StateVertex stateVertix) {
		return sfg.incomingEdgesOf(stateVertix);
	}

	/**
	 * Returns the set of outgoing states.
	 * 
	 * @param stateVertix
	 *            the state.
	 * @return the set of outgoing states from the stateVertix.
	 */
	public Set<StateVertex> getOutgoingStates(StateVertex stateVertix) {
		final Set<StateVertex> result = new HashSet<StateVertex>();

		for (Eventable c : getOutgoingClickables(stateVertix)) {
			result.add(sfg.getEdgeTarget(c));
		}

		return result;
	}

	/**
	 * @param clickable
	 *            the edge.
	 * @return the target state of this edge.
	 */
	public StateVertex getTargetState(Eventable clickable) {
		return sfg.getEdgeTarget(clickable);
	}

	/**
	 * Is it possible to go from s1 -> s2?
	 * 
	 * @param source
	 *            the source state.
	 * @param target
	 *            the target state.
	 * @return true if it is possible (edge exists in graph) to go from source to target.
	 */
	@GuardedBy("sfg")
	public boolean canGoTo(StateVertex source, StateVertex target) {
		synchronized (sfg) {
			return sfg.containsEdge(source, target) || sfg.containsEdge(target, source);
		}
	}

	/**
	 * Convenience method to find the Dijkstra shortest path between two states on the graph.
	 * 
	 * @param start
	 *            the start state.
	 * @param end
	 *            the end state.
	 * @return a list of shortest path of clickables from the state to the end
	 */
	public List<Eventable> getShortestPath(StateVertex start, StateVertex end) {
		return DijkstraShortestPath.findPathBetween(sfg, start, end);
	}

	/**
	 * Return all the states in the StateFlowGraph.
	 * 
	 * @return all the states on the graph.
	 */
	public Set<StateVertex> getAllStates() {
		return sfg.vertexSet();
	}

	/**
	 * Return all the edges in the StateFlowGraph.
	 * 
	 * @return a Set of all edges in the StateFlowGraph
	 */
	public Set<Eventable> getAllEdges() {
		return sfg.edgeSet();
	}

	/**
	 * Retrieve the copy of a state from the StateFlowGraph for a given StateVertix. Basically it
	 * performs v.equals(u).
	 * 
	 * @param state
	 *            the StateVertix to search
	 * @return the copy of the StateVertix in the StateFlowGraph where v.equals(u)
	 */
	private StateVertex getStateInGraph(StateVertex state) {
		Set<StateVertex> states = getAllStates();

		for (StateVertex st : states) {
			if (state.equals(st)) {
				return st;
			}
		}

		return null;
	}

	/**
	 * @return Dom string average size (byte).
	 */
	public int getMeanStateStringSize() {
		final Mean mean = new Mean();

		for (StateVertex state : getAllStates()) {
			mean.increment(state.getDomSize());
		}

		return (int) mean.getResult();
	}

	/**
	 * @return the state-flow graph.
	 */
	public DirectedGraph<StateVertex, Eventable> getSfg() {
		return sfg;
	}

	/**
	 * @param state
	 *            The starting state.
	 * @return A list of the deepest states (states with no outgoing edges).
	 */
	public List<StateVertex> getDeepStates(StateVertex state) {
		final Set<String> visitedStates = new HashSet<String>();
		final List<StateVertex> deepStates = new ArrayList<StateVertex>();

		traverse(visitedStates, deepStates, state);

		return deepStates;
	}

	private void traverse(Set<String> visitedStates, List<StateVertex> deepStates,
	        StateVertex state) {
		visitedStates.add(state.getName());

		Set<StateVertex> outgoingSet = getOutgoingStates(state);

		if ((outgoingSet == null) || outgoingSet.isEmpty()) {
			deepStates.add(state);
		} else {
			if (cyclic(visitedStates, outgoingSet)) {
				deepStates.add(state);
			} else {
				for (StateVertex st : outgoingSet) {
					if (!visitedStates.contains(st.getName())) {
						traverse(visitedStates, deepStates, st);
					}
				}
			}
		}
	}

	private boolean cyclic(Set<String> visitedStates, Set<StateVertex> outgoingSet) {
		int i = 0;

		for (StateVertex state : outgoingSet) {
			if (visitedStates.contains(state.getName())) {
				i++;
			}
		}

		return i == outgoingSet.size();
	}

	/**
	 * This method returns all possible paths from the index state using the Kshortest paths.
	 * 
	 * @param index
	 *            the initial state.
	 * @return a list of GraphPath lists.
	 */
	public List<List<GraphPath<StateVertex, Eventable>>> getAllPossiblePaths(StateVertex index) {
		final List<List<GraphPath<StateVertex, Eventable>>> results =
		        new ArrayList<List<GraphPath<StateVertex, Eventable>>>();

		final KShortestPaths<StateVertex, Eventable> kPaths =
		        new KShortestPaths<StateVertex, Eventable>(this.sfg, index, Integer.MAX_VALUE);
		// System.out.println(sfg.toString());

		for (StateVertex state : getDeepStates(index)) {
			// System.out.println("Deep State: " + state.getName());

			try {
				List<GraphPath<StateVertex, Eventable>> paths = kPaths.getPaths(state);
				results.add(paths);
			} catch (Exception e) {
				// TODO Stefan; which Exception is catched here???Can this be removed?
				LOGGER.error("Error with " + state.toString(), e);
			}

		}

		return results;
	}

	/**
	 * Return the name of the (new)State. By using the AtomicInteger the stateCounter is thread-safe
	 * 
	 * @return State name the name of the state
	 */
	public String getNewStateName() {
		stateCounter.getAndIncrement();
		String state = makeStateName(stateCounter.get(), efficientCrawling);
		return state;
	}

	/**
	 * Make a new state name given its id. Separated to get a central point when changing the names
	 * of states. The automatic state names start with "state" and guided ones with "guide".
	 * 
	 * @param id
	 *            the id where this name needs to be for.
	 * @return the String containing the new name.
	 */
	private String makeStateName(int id, boolean guided) {

		if (guided) {
			return "guided" + id;
		}

		return "state" + id;
	}
	
	/**
	 * Shabnam: removing a state from notFullExpandedStates list if all candidate clickables are fired. 
	 */
	public void removeFromNotFullExpandedStates(StateVertex s){
		if (notFullExpandedStates.contains(s)){
			notFullExpandedStates.remove(s);
			LOGGER.info("State " + s.getName() + " removed from the notFullExpandedStates list!");
		}
		else
			LOGGER.info("State " + s.getName() + " does not exist in the notFullExpandedStates list!");
	}
	
	//Shabnam
	public ArrayList<StateVertex> getNotFullExpandedStates(){
		return notFullExpandedStates;
	}
	
	/**
	 * Shabnam: Calculates the number of unprocessed candidate elements for all states in the graph
	 * 
	 * 
	 * @return the count of unprocessed candidate elements in the StateFlowGraph states
	 */
	public int getNumUnprocessedCandidateElements() {
		Set<StateVertex> states = getAllStates();
		int count = 0;
		
		for (StateVertex st : states) {
			count += st.getUnprocessedCandidateElements().size();
		}
		return count;
	}
	
	//shabnam
	public void setEfficientCrawling(boolean efficientCrawling) { 
		this.efficientCrawling = efficientCrawling;
	}
	

	
	//Shabnam

	public void updateExecutedFunctions(HashSet<String> executedFuncs){

		executedFunctions = executedFuncs;
	}	

	//Shabnam
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();
	//Shabnam
	private AstNode parse(String code) {
		
		Parser p = new Parser(compilerEnvirons, null);
		return p.parse(code, null, 0);
		
	}
	//shabnam
	private String getFunctionName(FunctionNode f) {
		if (f==null)
			return "NoFunctionNode";
	/*	else if(f.getParent() instanceof LabeledStatement){
			return ((LabeledStatement)f.getParent()).shortName();
		}
	*/	else if(f.getParent() instanceof ObjectProperty){
			return ((ObjectProperty)f.getParent()).getLeft().toSource();
		}
		Name functionName = f.getFunctionName();

		if (functionName == null) {
			return "anonymous" + f.getLineno();
		} else {
			return functionName.toSource();
		}
	}
	//Shabnam knownelems are the elements with onclick="function.." set in the html code
	//should be called in addstate (one time when a new state added)
	private void updateStatesPotentialFuncsWithKnownElems(StateVertex stateVertex){
		String state=stateVertex.toString();
		List<CandidateElement> candidateElems=stateVertex.getCandidateElemList();
		
		for(int i=0;i<candidateElems.size();i++){
			if(candidateElems.get(i).getElement().hasAttribute("onclick")){
				CandidateElement elem=candidateElems.get(i);
				String function=elem.getElement().getAttribute("onclick");
				AstNode funcNode=(AstNode) parse(function).getFirstChild();
				String funcName="";
				if(funcNode instanceof FunctionNode){
					funcName=getFunctionName((FunctionNode) funcNode);
				}
				else if(funcNode instanceof FunctionCall){
					funcName=((FunctionCall) funcNode).getTarget().toSource();
				}
				else if(funcNode instanceof PropertyGet){
					funcName=((PropertyGet)funcNode).getProperty().toSource();
				}
				
				ArrayList<Object> elemInfo=new ArrayList<Object>();
				elemInfo.add(elem);
				elemInfo.add(funcName);
				if(statesPotentialFuncs.get(state)!=null){
					statesPotentialFuncs.get(state).add(elemInfo);	
				}
				else{
					ArrayList<ArrayList<Object>> newList=new ArrayList<ArrayList<Object>>();
					newList.add(elemInfo);
					statesPotentialFuncs.put(state, newList);
				}
				updateStatesNewPotentialFuncs(state, funcName);
				
			}
		}
	}
	//Shabnam
	public void updateStatesPotentialFuncs(StateVertex stateVertex, TreeMap<String,ArrayList<ArrayList<Object>>> eventableElementsMap) throws SAXException, IOException{
		
		String state=stateVertex.toString();
		List<CandidateElement> candidateElems=stateVertex.getCandidateElemList();
		List<Eventable> eventableList= stateVertex.getCrawlPathToState();
		
	//	Document document=stateVertex.getDocument();
		Set<StateVertex> stateVertices=new HashSet<StateVertex>();
		stateVertices=getAllPredecessorVertices(stateVertex, stateVertices);
		Set<String> stateNames=new HashSet<String>();
		Iterator<StateVertex> verit=stateVertices.iterator();
		while(verit.hasNext()){
			StateVertex vertex=verit.next();
			stateNames.add(vertex.getName());
		}
		Set<String> keySet=eventableElementsMap.keySet();
		Iterator<String> it=keySet.iterator();
		while(it.hasNext()){
			String funcName= it.next();
			ArrayList<ArrayList<Object>> list= eventableElementsMap.get(funcName);
			for(int i=0;i<list.size();i++){
				ArrayList<Object> innerList=list.get(i);
				String id=(String) innerList.get(0);
				String vertex=(String) innerList.get(1);
				Eventable eventable=(Eventable) innerList.get(2);
				String eventType=(String) innerList.get(3);
				if(eventType.equals("unbind"))
					break;
				for(CandidateElement candidateElem:candidateElems){
				if(candidateElem.getElement().hasAttribute("id")){	
					if(candidateElem.getElement().getAttribute("id").equals(id)){
						

						ArrayList<Object> elemInfo=new ArrayList<Object>();
						elemInfo.add(candidateElem);
					//	elemInfo.add(document.getElementById(id));
						elemInfo.add(funcName);
						if(isRedundantItem(state,elemInfo))
							break;
						if(state.equals("index")){
							updateStatesPotentialFuncs_InitalState(state,funcName,candidateElem);
						}
					//	for(int j=0;j<eventableList.size();j++){
						//	if(eventableList.get(j).equals(eventable) || eventable==null){
							if(stateNames.contains(vertex)){
								if(!unbindedLater(stateVertex, vertex, candidateElem, funcName, eventableElementsMap)){
							
									if(statesPotentialFuncs.get(state)!=null){	
										statesPotentialFuncs.get(state).add(elemInfo);		
									}
									else{
										ArrayList<ArrayList<Object>> newList=new ArrayList<ArrayList<Object>>();
										newList.add(elemInfo);
										statesPotentialFuncs.put(state, newList);
									}
									updateStatesNewPotentialFuncs(state,funcName);
								}
							}
					//		}
					//	}
					}
				}
				}	
			}
		}
		
		updateStatesPotentialFuncsWithKnownElems(stateVertex);
	}

	//Shabnam
	public TreeMap<String,ArrayList<ArrayList<Object>>> getStatesPotentialFuncs(){
		return statesPotentialFuncs;
	}
	
	//Shabnam
	public TreeMap<String,HashSet<String>> getStatesNewPotentialFuncs(){
		return statesNewPotentialFuncs;
	}
	
	//Shabnam
	public int getStatesNewPotentialFuncs(StateVertex stateVertex){
		if(statesNewPotentialFuncs.get(stateVertex.toString())!=null){
			return statesNewPotentialFuncs.get(stateVertex.toString()).size();
		}
		return 0;
	}
	
	//Shabnam
	private void updateStatesNewPotentialFuncs(String stateVertex, String funcName){
	
		if(!executedFunctions.contains(funcName)){
			if(statesNewPotentialFuncs.get(stateVertex)!=null){
			
				statesNewPotentialFuncs.get(stateVertex).add(funcName);
				

			}
			else{
				HashSet<String> newList=new HashSet<String>();
				newList.add(funcName);
				statesNewPotentialFuncs.put(stateVertex, newList);
			}
			
		}
		//update according to static call graph
		updateNewPotentialFuncCall(stateVertex, funcName);
		
	}
	
	//shabnam
	private boolean isRedundantItem(String state,ArrayList<Object> elemInfo){
		
		if(statesPotentialFuncs.get(state)!=null){
			ArrayList<ArrayList<Object>> list=statesPotentialFuncs.get(state);
			for(int i=0;i<list.size();i++){
				ArrayList<Object> elementInfo=list.get(i);
				if(((CandidateElement)elementInfo.get(0)).getElement().getAttribute("id")
						.equals(((CandidateElement)elemInfo.get(0)).getElement().getAttribute("id"))){
					if(((String)elementInfo.get(1)).equals(elemInfo.get(1))){
						return true;
					}
				}
			}
		}
		return false;
	}
	// Shabnam get the number of all new potential functions for the given element
	public int getElementNewPotentialFuncs(StateVertex stateVertex, CandidateElement element){
		int newPotFuncs=0;
		String state=stateVertex.toString();
		if(statesPotentialFuncs.get(state)!=null){
			ArrayList<ArrayList<Object>> list=statesPotentialFuncs.get(state);
			for(int i=0;i<list.size();i++){
				ArrayList<Object> elemInfo=list.get(i);
				if((((CandidateElement)elemInfo.get(0)).getElement().getAttribute("id"))
						.equals(element.getElement().getAttribute("id"))){
					String funcName=(String) elemInfo.get(1);
					if(!executedFunctions.contains(funcName))
						newPotFuncs++;
					newPotFuncs+=getNewPotentialFuncCall(funcName);
					
					break;
				}
				
			}
			
		}
		return newPotFuncs;
	}
	
	//Shabnam
	private void updateNewPotentialFuncCall(String stateVertex,String funcName){
		ArrayList<Vertex> vertices=(ArrayList<Vertex>) StaticCallGraph.staticCallGraph.getVertices();
		for(int i=0;i<vertices.size();i++){
			if(vertices.get(i).name.equals(funcName)){
				ArrayList<Vertex> vertexList=new ArrayList<Vertex>();
				Vertex vertex=vertices.get(i);
				if(StaticCallGraph.staticCallGraph.getOutEdges(vertex).size()!=0){
					ArrayList<Vertex> successors=(ArrayList<Vertex>) StaticCallGraph.staticCallGraph.getAllSuccessorVertices(vertex, vertexList);
					for(Vertex v:successors){
						if(!executedFunctions.contains(v.name))
							statesNewPotentialFuncs.get(stateVertex).add(v.name);
					}
				}
					
				break;
			}
		}
		
	}
	
	//Shabnam
	private int getNewPotentialFuncCall(String funcName){
		int newPotFuncs=0;
		ArrayList<Vertex> vertices=(ArrayList<Vertex>) StaticCallGraph.staticCallGraph.getVertices();
		for(int i=0;i<vertices.size();i++){
			if(vertices.get(i).name.equals(funcName)){
				ArrayList<Vertex> vertexList=new ArrayList<Vertex>();
				Vertex vertex=vertices.get(i);
				if(StaticCallGraph.staticCallGraph.getOutEdges(vertex).size()!=0){
					ArrayList<Vertex> successors=(ArrayList<Vertex>) StaticCallGraph.staticCallGraph.getAllSuccessorVertices(vertex, vertexList);
					for(Vertex v:successors){
						if(!executedFunctions.contains(v.name))
							newPotFuncs++;
					}
				}
					
				break;
			}
		}
		return newPotFuncs;
		
	}
	
	//Shabnam returning the list of candidate elements with that are detected as real clickables
	public ArrayList<CandidateElement> getClickableElements(StateVertex stateVertex){
		HashSet<CandidateElement> elemList=new HashSet<CandidateElement>();
		String state=stateVertex.toString();
		if(statesPotentialFuncs.get(state)!=null){
			ArrayList<ArrayList<Object>> list=statesPotentialFuncs.get(state);
			for(int i=0;i<list.size();i++){
				ArrayList<Object> innerList=new ArrayList<Object>();
				innerList=list.get(i);
				CandidateElement elem=(CandidateElement) innerList.get(0);
				elemList.add(elem);
			}
		}
		Iterator<CandidateElement> it=elemList.iterator();
		ArrayList<CandidateElement> returnElemList=new ArrayList<CandidateElement>();
		while(it.hasNext()){
			returnElemList.add(it.next());
		}
		
		return returnElemList;
	}
	
	// Shabnam check whether potential functions remain the same between the current state and the previous one
	public boolean potenialFuncsRemainSame(StateVertex stateVertex){
		Set<StateVertex> prevStates = new HashSet<StateVertex>();
		Set<String> currStateFuncNames = new HashSet<String>();
		Set<String> prevStateFuncNames = new HashSet<String>();
		prevStates=getIncomingStates(stateVertex);
		currStateFuncNames=statesNewPotentialFuncs.get(stateVertex.toString());
		Iterator<StateVertex> it=prevStates.iterator();
		while(it.hasNext()){
			StateVertex vertex=it.next();
			prevStateFuncNames.addAll(statesNewPotentialFuncs.get(vertex.toString()));
			
		}
		if(currStateFuncNames.equals(prevStateFuncNames))
			return true;
		return false;
		
	
	}
	
	//Shabnam
	private Set<StateVertex> getIncomingStates(StateVertex stateVertix) {
		
		final Set<StateVertex> result = new HashSet<StateVertex>();

		for (Eventable c : getIncomingClickable(stateVertix)) {
			result.add(sfg.getEdgeSource(c));
		}

		return result;
	}
	
	//Shabnam
	private void updateStatesPotentialFuncs_InitalState(String state, String funcName,CandidateElement candidateElem){
	
			
				ArrayList<Object> elemInfo=new ArrayList<Object>();
				elemInfo.add(candidateElem);
			//	elemInfo.add(document.getElementById(id));
				elemInfo.add(funcName);
				if(isRedundantItem(state,elemInfo))
					return;
				if(statesPotentialFuncs.get(state)!=null){	
					statesPotentialFuncs.get(state).add(elemInfo);		
				}
				else{
					ArrayList<ArrayList<Object>> newList=new ArrayList<ArrayList<Object>>();
					newList.add(elemInfo);
					statesPotentialFuncs.put(state, newList);
				}
				updateStatesNewPotentialFuncs(state,funcName);
			
		
	}
	
	//Shabnam
/*	private boolean unbindedLater(int eventableIndex, ArrayList<Eventable> eventableList, CandidateElement elem, String funcName,
			TreeMap<String,ArrayList<ArrayList<Object>>> eventableElementsMap){
		ArrayList<ArrayList<Object>> list= eventableElementsMap.get(funcName);

		
			for(int i=eventableIndex;i<eventableList.size();i++){
				for(int j=0;j<list.size();j++){
					ArrayList<Object> innerList=list.get(j);
					String id=(String) innerList.get(0);
					Eventable eventable=(Eventable) innerList.get(2);
					String eventType=(String) innerList.get(3);
					if(eventableList.get(i).equals(eventable) &&
							eventType.equals("unbind"))
						if(elem.getElement().hasAttribute("id") 
								&& elem.getElement().getAttribute("id").equals(id)){
							return true;
						}
				}
			}
			
			return false;

					
		
	}
*/	
	//Shabnam
	private Set<StateVertex> getAllPredecessorVertices(StateVertex stateVertex,Set<StateVertex> stateVertices){
		
		Set<Eventable> eventable=sfg.incomingEdgesOf(stateVertex);
		if(eventable.size()!=0){
			
			Iterator<Eventable> it=eventable.iterator();
			while(it.hasNext()){
				getAllPredecessorVertices(it.next().getSourceStateVertex(),stateVertices);
			}
		}
		else
			stateVertices.add(stateVertex);
		return stateVertices;
			
	}
	
	//Shabnam
	private Set<StateVertex> getAllSuccessorVertices(StateVertex stateVertex,Set<StateVertex> stateVertices){
		
		Set<Eventable> eventable=sfg.outgoingEdgesOf(stateVertex);
		if(eventable.size()!=0){
			
			Iterator<Eventable> it=eventable.iterator();
			while(it.hasNext()){
				getAllPredecessorVertices(it.next().getTargetStateVertex(),stateVertices);
			}
		}
		else
			stateVertices.add(stateVertex);
		return stateVertices;
			
	}
	
	//Shabnam
	private boolean unbindedLater(StateVertex curStateVertex, String stateInPath, CandidateElement elem, String funcName,
			TreeMap<String,ArrayList<ArrayList<Object>>> eventableElementsMap){
		
		Set<StateVertex> allstates=new HashSet<StateVertex>();
		allstates=this.getAllStates();
		Iterator<StateVertex> iter=allstates.iterator();
		StateVertex vertexInPath = null;
		while(iter.hasNext()){
			StateVertex st=iter.next();
			if(st.getName().equals(stateInPath)){
				vertexInPath=st;
			}
		}
		
		ArrayList<ArrayList<Object>> list= eventableElementsMap.get(funcName);

		Set<StateVertex> successorStateVertices=new HashSet<StateVertex>();
		Set<StateVertex> preStateVertices=new HashSet<StateVertex>();
		preStateVertices=getAllPredecessorVertices(curStateVertex, preStateVertices);
		successorStateVertices=getAllSuccessorVertices(vertexInPath, preStateVertices);
		
		Set<String> successorStateNames=new HashSet<String>();
		Iterator<StateVertex> verit=successorStateVertices.iterator();
		while(verit.hasNext()){
			StateVertex vertex=verit.next();
			successorStateNames.add(vertex.getName());
		}
		Set<String> preStateNames=new HashSet<String>();
		Iterator<StateVertex> preVerIt=preStateVertices.iterator();
		while(preVerIt.hasNext()){
			StateVertex vertex=preVerIt.next();
			successorStateNames.add(vertex.getName());
		}
	
				for(int j=0;j<list.size();j++){
					ArrayList<Object> innerList=list.get(j);
					String id=(String) innerList.get(0);
					String stv=(String) innerList.get(1);
		//			Eventable eventable=(Eventable) innerList.get(2);
					String eventType=(String) innerList.get(3);
					if(successorStateNames.contains(stv) && preStateNames.contains(stv) &&
							eventType.equals("unbind"))
						if(elem.getElement().hasAttribute("id") 
								&& elem.getElement().getAttribute("id").equals(id)){
							return true;
						}
				}
			
			
			return false;

					
		
	}
	
		
	
}
