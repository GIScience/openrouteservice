//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.11.29 um 04:11:24 PM CET 
//


package heigit.ors.services.routing.requestprocessors.gpx.beans;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.topografix.gpx._1._1 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Gpx_QNAME = new QName("http://www.topografix.com/GPX/1/1", "gpx");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.topografix.gpx._1._1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Gpx }
     * 
     */
    public Gpx createGpxType() {
        return new Gpx();
    }

    /**
     * Create an instance of {@link TrkType }
     * 
     */
    public TrkType createTrkType() {
        return new TrkType();
    }

    /**
     * Create an instance of {@link BoundsType }
     * 
     */
    public BoundsType createBoundsType() {
        return new BoundsType();
    }

    /**
     * Create an instance of {@link CopyrightType }
     * 
     */
    public CopyrightType createCopyrightType() {
        return new CopyrightType();
    }

    /**
     * Create an instance of {@link RteType }
     * 
     */
    public RteType createRteType() {
        return new RteType();
    }

    /**
     * Create an instance of {@link TrksegType }
     * 
     */
    public TrksegType createTrksegType() {
        return new TrksegType();
    }

    /**
     * Create an instance of {@link EmailType }
     * 
     */
    public EmailType createEmailType() {
        return new EmailType();
    }

    /**
     * Create an instance of {@link PtType }
     * 
     */
    public PtType createPtType() {
        return new PtType();
    }

    /**
     * Create an instance of {@link PtsegType }
     * 
     */
    public PtsegType createPtsegType() {
        return new PtsegType();
    }

    /**
     * Create an instance of {@link LinkType }
     * 
     */
    public LinkType createLinkType() {
        return new LinkType();
    }

    /**
     * Create an instance of {@link WptType }
     * 
     */
    public WptType createWptType() {
        return new WptType();
    }

    /**
     * Create an instance of {@link ExtensionsType }
     * 
     */
    public ExtensionsType createExtensionsType() {
        return new ExtensionsType();
    }

    /**
     * Create an instance of {@link PersonType }
     * 
     */
    public PersonType createPersonType() {
        return new PersonType();
    }

    /**
     * Create an instance of {@link MetadataType }
     * 
     */
    public MetadataType createMetadataType() {
        return new MetadataType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Gpx }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.topografix.com/GPX/1/1", name = "gpx")
    public JAXBElement<Gpx> createGpx(Gpx value) {
        return new JAXBElement<Gpx>(_Gpx_QNAME, Gpx.class, null, value);
    }

}
