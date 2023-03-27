package org.heigit.ors.api.responses.matrix;

import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.matrix.MatrixResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MatrixResponseInfoTest {
    private static MatrixRequest bareMatrixRequest;
    private MatrixResponseInfo responseInformation;

    @BeforeEach
    void setUp() {
        bareMatrixRequest = new MatrixRequest(new ArrayList<>());
        MatrixResponse bareMatrixResponse = new MatrixResponse(new MatrixResult(null, null), bareMatrixRequest);
        responseInformation = bareMatrixResponse.responseInformation;
    }

    @Test
    void expectEngineInfoTest() {
        assertEquals(MatrixResponseInfo.class, responseInformation.getClass());
    }


    @Test
    void getAttributionTest() {
        assertEquals(MatrixResponseInfo.class, responseInformation.getClass());
        assertEquals(String.class, responseInformation.getAttribution().getClass());
    }

    @Test
    void getServiceTest() {
        assertEquals("matrix", responseInformation.getService());
    }

    @Test
    void getTimeStampTest() {
        assertTrue(Long.toString(responseInformation.getTimeStamp()).length() > 0);

    }

    @Test
    void getRequestTest() {
        assertEquals(bareMatrixRequest, responseInformation.getRequest());
    }

    @Test
    void getEngineInfoTest() {
        assertNotNull(responseInformation.getEngineInfo());
    }
}
