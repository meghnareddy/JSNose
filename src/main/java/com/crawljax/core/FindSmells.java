package com.crawljax.core;

import java.util.HashSet;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;

import codesmells.SmellDetector;

/**
 * The Class FindSmells.
 */
public class FindSmells {

	private HashSet<String> jsInTag = new HashSet<>();
	
	private SmellDetector smellDetector = new SmellDetector();

	public void findSmellsInCode(String scopename, String input) {

		String jsName = getJSName(scopename);

		SmellDetector.setJSName(jsName);

		if (scopename.contains("script")) {
			
			SmellDetector.analyseCoupling(jsName, input, jsInTag);
		} else if (true) {
			SmellDetector.analyseCoupling("main_html", "", jsInTag);
		}
		
		AstNode ast = null;
		
//		CompilerEnvirons env = new CompilerEnvirons();
//
//		env.setRecoverFromErrors(true);
//		IRFactory factory = new IRFactory(env);
//		AstRoot rootNode = factory.parse(input, null, 0);

		
		/* create a new parser */
		Parser rhinoParser = new Parser(new CompilerEnvirons(), Context.enter().getErrorReporter());

		/* parse some script and save it in AST */
		ast = rhinoParser.parse(input , jsName, 1);
		
		smellDetector.SetASTNode(ast);
		smellDetector.analyseAstNode();		
		
	}

	// Amin: This is used to name the array which stores execution count for the
	// scope in URL
	private String getJSName(String URL) {
		int index = URL.lastIndexOf('/');
		String s = URL.substring(index + 1, URL.length());
		String finalString = s.replace('.', '_');
		return finalString;
	}
}
