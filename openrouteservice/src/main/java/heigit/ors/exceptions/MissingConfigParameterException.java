package heigit.ors.exceptions;

public class MissingConfigParameterException {

    public MissingConfigParameterException(String missingConfigParameter) {
        System.err.print("Config parameter " + missingConfigParameter + " is missing." + "\n");
    }
}
