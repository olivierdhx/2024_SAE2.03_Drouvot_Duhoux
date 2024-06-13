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
    public static String port = "";
    public static String link = "";
    public static String index = "";
    public static String accept = null;
    public static String reject = null;

    public static String accesLog = "";
    public static String errorLog = "";

    public ConfigServ(String pathConfig){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(pathConfig));
            NodeList list = doc.getElementsByTagName("webconf");
            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    port = element.getElementsByTagName("port").item(0).getTextContent();
                    System.out.println("Port = " + port);
                    link = element.getElementsByTagName("root").item(0).getTextContent();
                    System.out.println("Link = " + link);
                    index = element.getElementsByTagName("index").item(0).getTextContent();
                    System.out.println("Index = " + index);
                    accept = element.getElementsByTagName("accept").item(0).getTextContent();
                    System.out.println("Accept = " + accept);
                    reject = element.getElementsByTagName("reject").item(0).getTextContent();
                    System.out.println("Reject = " + reject);
                    accesLog = element.getElementsByTagName("acceslog").item(0).getTextContent();
                    System.out.println("AccesLog = " + accesLog);
                    errorLog = element.getElementsByTagName("errorlog").item(0).getTextContent();
                    System.out.println("ErrorLog = " + errorLog);
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
