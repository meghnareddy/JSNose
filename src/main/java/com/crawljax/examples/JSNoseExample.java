package com.crawljax.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.FindSmells;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.configuration.ThreadConfiguration;
import com.crawljax.plugins.aji.JSASTModifier;
import com.crawljax.plugins.aji.JSModifyProxyPlugin;
import com.crawljax.plugins.aji.executiontracer.AstInstrumenter;
import com.crawljax.plugins.webscarabwrapper.WebScarabWrapper;

/**
 * Simple JSNose Example.
 * 
 * @author aminmf@ece.ubc.ca (Amin Milani Fard)
 * @version $id$
 */
public final class JSNoseExample {

	// Our settings for the SCAM paper
	private static boolean doDiverseCrawling = true; // default should be false
	private static boolean doEfficientCrawling = false; // default should be false
	private static boolean doRandomEventExec = false; // set it true for randomizing event execution
	private static boolean doClickOnce = true; // true: click only once on each clickable, false: multiple click

	private static final String URL = "http://localhost:3333/swagger-ui/";

	private static final int MAX_NUMBER_STATES = 10000;
	private static final int MAX_RUNTIME = 600;
	private static final int MAX_DEPTH = 0; // this indicates no depth-limit

	private JSNoseExample() {
		JSASTModifier.innstrumentForCoverage(URL);
	}
	
	/**
	 * @param args the command line args
	 */
	public static void main(String[] args) {
		
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream is = cl.getResourceAsStream("jsnose.properties");
		Properties p = new Properties();
		
		try {
			p.load(is);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Missing Properties ... ");
			System.exit(1);
		}
		String webdriver = p.getProperty("webdriver.chrome.driver");
		System.setProperty("webdriver.chrome.driver", webdriver);
		System.setProperty("webdriver.gecko.driver", p.getProperty("webdriver.gecko.driver"));

		try {
			
			runAnalyse();
//			CrawljaxController crawljax = new CrawljaxController(getCrawljaxConfiguration());
//			crawljax.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static void runAnalyse() throws IOException {
		
		WebDriver driver = new ChromeDriver();
		driver.get("http://localhost:3333/swagger-ui/");
		WebElement scritTag = driver.findElement(By.tagName("script"));
		String htmlCode = scritTag.getAttribute("src");
		
		FindSmells findSmells = new FindSmells();
		
		java.net.URL url = new java.net.URL(htmlCode);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		
		StringBuilder content;

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {

            String line;
            content = new StringBuilder();

            while ((line = in.readLine()) != null) {

                content.append(line);
                content.append(System.lineSeparator());
            }
        }

		findSmells.findSmellsInCode(htmlCode, content.toString());
		
		
	}

	private static CrawljaxConfiguration getCrawljaxConfiguration() {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setCrawlSpecification(getCrawlSpecification());
		config.setThreadConfiguration(getThreadConfiguration());
		config.setBrowser(BrowserType.chrome);

		// Amin: Create a Proxy for the purpose of code instrumentation
		config.setProxyConfiguration(new ProxyConfiguration());
		WebScarabWrapper web = new WebScarabWrapper();
		config.addPlugin(web);
		JSModifyProxyPlugin modifier = new JSModifyProxyPlugin(new AstInstrumenter());
		modifier.excludeDefaults();
		web.addPlugin(modifier);

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

		crawler.setMaximumRuntime(MAX_RUNTIME);

		crawler.setDiverseCrawling(doDiverseCrawling); // setting the guided crawling
		crawler.setClickOnce(doClickOnce); // setting if should click once/multiple time on each clickable

		// doRandomEventExec and doEfficientCrawling should not be both true!
		if (doRandomEventExec) {
			crawler.setRandomEventExec(true);
		} else if (doEfficientCrawling) {
			crawler.setEfficientCrawling(true);
			crawler.setClickOnce(false);
		}

		if (URL.equals("http://localhost/Tunnel/")) {
			crawler.click("p").withAttribute("id", "welcome");
		} else if (URL.equals("http://localhost/GhostBusters/")) {
			crawler.click("div").withAttribute("data-m", "%").withAttribute("id", "g%");
			crawler.dontClick("div").withAttribute("id", "end").withAttribute("backgroundColor", "#fff");

		} else if (URL.equals("http://localhost:8080/tudu-dwr/")) {
			// this is just for the TuduList application
			Form form = new Form();
			Form addList = new Form();
			form.field("j_username").setValue("amin");
			form.field("j_password").setValue("editor");
			form.field("dueDate").setValue("10/10/2010");
			form.field("priority").setValue("10");
			// addList.field("description").setValue("test");
			InputSpecification input = new InputSpecification();
			input.setValuesInForm(form).beforeClickElement("input").withAttribute("type", "submit");
			input.setValuesInForm(addList).beforeClickElement("a").withAttribute("href", "javascript:addTodo();");
			crawler.setInputSpecification(input);
			crawler.click("a");
			crawler.click("img").withAttribute("id", "add_trigger_calendar");
			crawler.click("img").withAttribute("id", "edit_trigger_calendar");

			// crawler.click("a");
			crawler.click("div");
			crawler.click("span");
			crawler.click("img");
			// crawler.click("input").withAttribute("type", "submit");
			crawler.click("td");

			crawler.dontClick("a").withAttribute("title", "My info");
			crawler.dontClick("a").withAttribute("title", "Log out");
			crawler.dontClick("a").withAttribute("text", "Cancel");
		} else {
			// default order of clicks on candidate clickable
			crawler.clickDefaultElements();
			crawler.click("a");
			crawler.click("div");
			crawler.click("span");
			crawler.click("img");
			crawler.click("button");
			crawler.click("input").withAttribute("type", "submit");
			crawler.click("td");
		}

		// except these
		crawler.dontClick("a").underXPath("//DIV[@id='guser']");
		crawler.dontClick("a").withText("Language Tools");

		if (!URL.equals("http://localhost:8080/tudu-dwr/"))
			crawler.setInputSpecification(getInputSpecification());

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
}
