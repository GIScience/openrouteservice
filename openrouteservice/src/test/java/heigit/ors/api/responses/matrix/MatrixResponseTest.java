package heigit.ors.api.responses.matrix;

import heigit.ors.api.requests.matrix.MatrixRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MatrixResponseTest {
    private MatrixResponse bareMatrixResponse;


    @Before
    public void setUp() {
        MatrixRequest bareMatrixRequest = new MatrixRequest();
        bareMatrixResponse = new MatrixResponse(bareMatrixRequest);
    }

    @Test
    public void getResponseInformation() {

        Assert.assertEquals(MatrixResponseInfo.class, bareMatrixResponse.responseInformation.getClass());
        Assert.assertNotNull(bareMatrixResponse.responseInformation);
    }

    @Test
    public void getMatrixResults() {
        Assert.assertNull(bareMatrixResponse.getMatrixResults());
    }
}