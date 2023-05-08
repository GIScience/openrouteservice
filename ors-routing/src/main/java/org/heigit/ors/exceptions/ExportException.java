package org.heigit.ors.exceptions;


import org.heigit.ors.common.ServiceRequest;
import org.heigit.ors.common.StatusCode;

/**
 * The class handles the error reporting whenever an invalid service/export combination is called.
 *
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class ExportException extends StatusCodeException {
    private static final long serialVersionUID = 59778833258948573L;

    /**
     * @param errorCode         Represents the the error code as described in the "error_codes.md"
     * @param toBeExportedClass Represents the specific {@link ServiceRequest} class. E.g. geocodingRequest.getClass()
     * @param exportMethod      Represents the export format as a {@link String}
     */
    public ExportException(int errorCode, Class rootClass, Class toBeExportedClass, String exportMethod) {
        super(StatusCode.NOT_IMPLEMENTED, errorCode, toBeExportedClass.getName() + " can not (yet) be exported through " + rootClass + " using export method " + exportMethod);
    }

    /**
     * @param errorCode         Represents the the error code as described in the "error_codes.md"
     * @param toBeExportedClass Represents the specific {@link ServiceRequest} class. E.g. geocodingRequest.getClass()
     * @param exportMethod      Represents the export format as a {@link String}. E.g. "GeoJSON" or "JSON"
     */
    public ExportException(int errorCode, Class toBeExportedClass, String exportMethod) {
        super(StatusCode.INTERNAL_SERVER_ERROR, errorCode, "An error accured exporting " + toBeExportedClass.getName() + " using " + exportMethod);
    }
}
