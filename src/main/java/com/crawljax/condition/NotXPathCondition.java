package com.crawljax.condition;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Condition that returns true iff no elements are found with expression.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id: NotXPathCondition.java 6301 2009-12-24 16:36:24Z mesbah $
 */
public class NotXPathCondition extends AbstractCondition {

	private final XPathCondition xpathCondition;

	/**
	 * @param expression
	 *            the XPath expression.
	 */
	public NotXPathCondition(String expression) {
		this.xpathCondition = new XPathCondition(expression);
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		return Logic.not(xpathCondition).check(browser);
	}

}