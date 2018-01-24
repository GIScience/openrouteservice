package heigit.ors.exceptions;

import org.apache.log4j.Logger;


/**
 * {@link MissingConfigParameterException} provides two Methods to print access errors with the app.config using {@link Logger}.
 */
public class MissingConfigParameterException {

    /**
     * The constructor raises an Logger.info using log4j with an unique message attached.
     * Input example: "new MissingConfigParameterException(LOGGER, "support_mail");"
     *
     * @param logger                 A {@link Logger} object containing the related class.
     * @param missingConfigParameter The parameter should represent the App.config parameter that raised an access error
     */
    public MissingConfigParameterException(Logger logger, String missingConfigParameter) {
        logger.info("Missing config parameter " + missingConfigParameter);
    }

    /**
     * The constructor raises an Logger.error using log4j with a unique message attached.
     * Input example: "new MissingConfigParameterException(LOGGER, "support_mail", "Support_mail seems to be malformed");"
     *
     * @param logger                 A {@link Logger} object containing the related class.
     * @param missingConfigParameter The parameter should represent the App.config parameter that raised an access error
     * @param message                The parameter can be a unique error message that is not or too unique to be integrated as its own error class/method
     */
    public MissingConfigParameterException(Logger logger, String missingConfigParameter, String message) {
        logger.error("The config parameter " + missingConfigParameter + " raised an error with the following message: " + message);
    }
}
