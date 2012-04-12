package bigs.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bigs.core.BIGS;
import bigs.core.utils.Core;



public class GetJNLPServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest theRequest, HttpServletResponse theResponse)
        throws IOException
    {
		PrintWriter pw = theResponse.getWriter();

        theResponse.setContentType("application/jnlp");
        theResponse.setHeader("Content-disposition", "attachment; filename=bigs-worker-"+Web.thisHost+"-"+Web.thisPort+".jnlp");
        
        // gets jnlp file template and inserts encrypted BIGS properties and codebase
        String jnlpFile = Core.extractResourceAsString("bigs-worker.jnlp");
        String encryptedProperties = Core.encrypt(BIGS.globalProperties.toString());        
        String codebase = "http://"+Web.thisHost+":"+Web.thisPort;
        jnlpFile = jnlpFile.replaceAll("@ENCRIPTEDPROPERTIES@", encryptedProperties);                
        jnlpFile = jnlpFile.replaceAll("@CODEBASE@", codebase);
        jnlpFile = jnlpFile.replaceAll("@RELEASE@", BIGS.bigsRelease);
        pw.println(jnlpFile);        

    }

}