package org.heigit.ors.api.responses.matrix;

import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.matrix.MatrixResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class MatrixResponseTest {
    private MatrixResponse bareMatrixResponse;


    @Before
    public void setUp() {
        System.setProperty("ors_config", "target/test-classes/ors-config-test.json");

        MatrixRequest bareMatrixRequest = new MatrixRequest(new ArrayList<>());
        bareMatrixResponse = new MatrixResponse(new MatrixResult(null, null), bareMatrixRequest);
    }

    @Test
    public void getResponseInformation() {

        Assert.assertEquals(MatrixResponseInfo.class, bareMatrixResponse.responseInformation.getClass());
        Assert.assertNotNull(bareMatrixResponse.responseInformation);
    }
}
