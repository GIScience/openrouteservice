package heigit.ors.services.common;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

public class Utils {

	public synchronized static final String getORSVersion() {

		String version = null;

		try {
			String curDir = System.getProperty("user.dir");
			Path pomFile = Paths.get(Paths.get(curDir).getParent().toString(), "openrouteservice").resolve("pom.xml");
			
			try (InputStream is = Files.newInputStream(pomFile)) 
			{
				Document doc = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder().parse(is);
				doc.getDocumentElement().normalize();
				version = (String) XPathFactory.newInstance().newXPath().compile("/project/version").evaluate(doc, XPathConstants.STRING);
				if (version != null) {
					version = version.trim();
				}
			}
		} catch (Exception e) {
		}

		return version;
	}
}
