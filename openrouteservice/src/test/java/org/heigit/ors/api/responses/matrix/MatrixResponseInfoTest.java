package heigit.ors.api.responses.matrix;

import heigit.ors.api.requests.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class MatrixResponseInfoTest {
    private static MatrixRequest bareMatrixRequest;
    private MatrixResponseInfo responseInformation;

    @Before
    public void setUp() {
        bareMatrixRequest = new MatrixRequest(new ArrayList<>());
        MatrixResponse bareMatrixResponse = new MatrixResponse(new MatrixResult(null, null), bareMatrixRequest);
        responseInformation = bareMatrixResponse.responseInformation;
    }

    @Test
    public void expectEngineInfoTest() {
        Assert.assertEquals(MatrixResponseInfo.class, responseInformation.getClass());
    }


    @Test
    public void getAttributionTest() {
        Assert.assertEquals(MatrixResponseInfo.class, responseInformation.getClass());
        Assert.assertEquals(String.class, responseInformation.getAttribution().getClass());
    }

    @Test
    public void getServiceTest() {
        Assert.assertEquals("matrix", responseInformation.getService());
    }

    @Test
    public void getTimeStampTest() {
        Assert.assertTrue(Long.toString(responseInformation.getTimeStamp()).length() > 0);

    }

    @Test
    public void getRequestTest() {
        Assert.assertEquals(bareMatrixRequest, responseInformation.getRequest());
    }

    @Test
    public void getEngineInfoTest() {
        Assert.assertNotNull(responseInformation.getEngineInfo());
    }
}