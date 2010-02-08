package com.tinkerpop.webling.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinkerpop.gremlin.GremlinEvaluator;
import com.tinkerpop.webling.WeblingLauncher;
	
/**
 * @author Pavel A. Yaskevich
 */
public class EvaluatorServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final String newLineRegex = "(\r\n|\r|\n|\n\r)";
    
    @SuppressWarnings("unchecked")
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext sc = getServletContext();
        String code = request.getParameter("code");
        String logMessage = "[POST /exec?code=" + code.replaceAll(newLineRegex, " ") + "] ";

        if (code.isEmpty()) {
            sc.log(logMessage + "400 ERROR");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
          
        String sessionId = request.getSession(true).getId();
        GremlinEvaluator gremlin = WeblingLauncher.getEvaluatorBySessionId(sessionId);
          
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);
        
        // log request
        sc.log(logMessage + "200 OK");

        // redirecting standart output to our custom printStream
        // to be able to show user result of g:print() function
        PrintStream out = new PrintStream(response.getOutputStream());
        System.setOut(out);
        
        try {
            List result = gremlin.evaluate(new ByteArrayInputStream(code.getBytes()));
            out.println("==> " + ((result.size() == 1) ? result.get(0) : result));
        } catch(Exception e) {
            out.println(e.getMessage());
        }
          
        out.close();
    }
	
}
