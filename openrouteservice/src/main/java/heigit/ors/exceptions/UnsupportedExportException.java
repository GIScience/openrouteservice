package heigit.ors.exceptions;


import org.apache.log4j.Logger;

/**
 * {@link UnsupportedExportException} should be used in {@link heigit.ors.GlobalResponseProcessor.GlobalResponseProcessor}.
 * {@link UnsupportedExportException} provides one constructor to print unsupported export combinations using {@link Logger}.
 *
 * @author Julian Psotta
 */
public class UnsupportedExportException {
    /**
     * @param rootClass         Represents the class where the error is raised. E.g. "this.getClass()"
     * @param toBeExportedClass Represents the specific {@link heigit.ors.services.ServiceRequest} class. E.g. geocodingRequest.getClass()
     * @param exportName        Represents the export format as a {@link String}
     */
    public UnsupportedExportException(Class rootClass, Class toBeExportedClass, String exportName) {
        final Logger logger = Logger.getLogger(rootClass.getName());
        logger.error(toBeExportedClass.getName() + " can not (yet) be exported through " + rootClass + " using " + exportName + " as an export method");
    }
}
