import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class ConfigServ {
    private static String port = "";
    private static String link = "";
    private static String index = "";
    private static String accept = null;
    private static String reject = null;

    private static String accesLog = "";
    private static String errorLog = "";

    /**
     * Rempli la config a partir du path
     * @param pathConfig path du fichier config xml
     */

    public ConfigServ(String pathConfig){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            //Lecture de chaque balise
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(pathConfig));
            NodeList list = doc.getElementsByTagName("webconf");
            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    port = element.getElementsByTagName("port").item(0).getTextContent();
                    link = element.getElementsByTagName("root").item(0).getTextContent();
                    index = element.getElementsByTagName("index").item(0).getTextContent();
                    accept = element.getElementsByTagName("accept").item(0).getTextContent();
                    reject = element.getElementsByTagName("reject").item(0).getTextContent();
                    accesLog = element.getElementsByTagName("acceslog").item(0).getTextContent();
                    errorLog = element.getElementsByTagName("errorlog").item(0).getTextContent();
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }


    public String getPort() {
        return port;
    }

    public String getLink() {
        return link;
    }

    public String getIndex() {
        return index;
    }

    public String getAccept() {
        return accept;
    }

    public String getReject() {
        return reject;
    }

    public String getAccesLog() {
        return accesLog;
    }

    public String getErrorLog() {
        return errorLog;
    }
}
