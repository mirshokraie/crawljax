package com.crawljax.core.plugin;

import java.util.List;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.Eventable;

public interface OnFireEventSuccessPlugin extends Plugin {

		/**
		 * Method that is called when an event that was requested to fire failed firing.
		 * 
		 * @param eventable
		 *            the eventable that failed to execute
		 * @param pathToFailure
		 *            the list of eventable lead TO this failed eventable, the eventable excluded.
		 */
		void onFireEventSuccessed(Eventable eventable, List<Eventable> pathToFailure, CrawlSession session);

}
