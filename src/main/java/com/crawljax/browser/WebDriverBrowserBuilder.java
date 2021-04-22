package com.crawljax.browser;

import java.util.List;

import org.openqa.selenium.android.AndroidDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.iphone.IPhoneDriver;

import com.crawljax.core.configuration.CrawljaxConfigurationReader;

/**
 * This class represents the default Crawljax used implementation of the BrowserBuilder. It's based
 * on the WebDriver implementations offered by Crawljax.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: WebDriverBrowserBuilder.java 465M 2012-05-04 20:42:33Z (local) $
 */
public class WebDriverBrowserBuilder implements EmbeddedBrowserBuilder {

	/**
	 * Build a new WebDriver based EmbeddedBrowser.
	 * 
	 * @see EmbeddedBrowserBuilder#buildEmbeddedBrowser(CrawljaxConfigurationReader)
	 * @param configuration
	 *            the configuration object to read the config values from
	 * @return the new build WebDriver based embeddedBrowser
	 */
	@Override
	public EmbeddedBrowser buildEmbeddedBrowser(CrawljaxConfigurationReader configuration) {
		// Retrieve the config values used
		List<String> filterAttributes = configuration.getFilterAttributeNames();
		int crawlWaitReload = configuration.getCrawlSpecificationReader().getWaitAfterReloadUrl();
		int crawlWaitEvent = configuration.getCrawlSpecificationReader().getWaitAfterEvent();

		// Determine the requested browser type
		switch (configuration.getBrowser()) {
			
			case chrome:
			WebDriverBackedEmbeddedBrowser withDriver = WebDriverBackedEmbeddedBrowser.withDriver(new ChromeDriver(),
			        configuration.getFilterAttributeNames(), configuration
			                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
			                .getCrawlSpecificationReader().getWaitAfterReloadUrl());
			
			
			return withDriver;

			
		}
		return null;
	}
}
