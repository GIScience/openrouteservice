package heigit.ors.exceptions;

import org.apache.log4j.Logger;


/**
 * {@link MissingConfigParameterException} provides two Constructors to print access errors with the app.config using {@link Logger}.
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class MissingConfigParameterException {

    /**
     * The constructor raises an Logger.error using log4j with an unique message attached.
     * Input example: "new MissingConfigParameterException(this.getClass(), "support_mail");"
     *
     * @param originalClass                 A {@link Class} object containing the related class.
     * @param missingConfigParameter The parameter should represent the App.config parameter that raised an access error
     */
    public MissingConfigParameterException(Class originalClass, String missingConfigParameter) {
        final Logger logger = Logger.getLogger(originalClass.getName());
        logger.error("Missing config parameter " + missingConfigParameter);
    }

    /**
     * The constructor raises an Logger.error using log4j with a unique message attached.
     * Input example: "new MissingConfigParameterException(this.getClass(), "support_mail", "The parameter seems to be malformed");"
     * @param originalClass                 A {@link Class} object containing the related class.
     * @param missingConfigParameter The parameter should represent the App.config parameter that raised an access error
     */
    public MissingConfigParameterException(Class originalClass, String missingConfigParameter, String message) {
        final Logger logger = Logger.getLogger(originalClass.getName());
        logger.error("The config parameter " + missingConfigParameter + " raised an error with the following message: " + message);
    }
}
