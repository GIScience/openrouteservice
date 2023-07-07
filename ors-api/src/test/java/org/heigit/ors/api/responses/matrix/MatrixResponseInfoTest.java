package org.heigit.ors.api.responses.matrix;

import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.SystemMessageProperties;
import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.matrix.MatrixResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("unittest")
class MatrixResponseInfoTest {
    private static MatrixRequest bareMatrixRequest;
    private MatrixResponseInfo responseInformation;
    @Autowired
    private final SystemMessageProperties systemMessageProperties = new SystemMessageProperties();
    @Autowired
    private final EndpointsProperties endpointsProperties = new EndpointsProperties();

    @BeforeEach
    void setUp() {
        bareMatrixRequest = new MatrixRequest(new ArrayList<>());
        MatrixResponse bareMatrixResponse = new MatrixResponse(new MatrixResult(null, null), bareMatrixRequest, systemMessageProperties, endpointsProperties);
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
