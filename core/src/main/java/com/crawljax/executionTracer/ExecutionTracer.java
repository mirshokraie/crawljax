package com.crawljax.executionTracer;

import com.crawljax.graph.WeightedGraph;

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
import com.crawljax.core.plugin.OnFireEventSuccessPlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateMachine;
import com.crawljax.util.Helper;

public abstract class ExecutionTracer
				implements OnFireEventSuccessPlugin/*PreStateCrawlingPlugin, OnNewStatePlugin */{
	
	
	protected static final int ONE_SEC = 1000;

	protected static JSONArray points = new JSONArray();



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
			buffer = new JSONArray(string);
			for (int i = 0; i < buffer.length(); i++) {
				points.put(buffer.get(i));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}


}
