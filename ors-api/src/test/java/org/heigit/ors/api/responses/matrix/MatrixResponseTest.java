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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("unittest")
class MatrixResponseTest {
    private MatrixResponse bareMatrixResponse;
    @Autowired
    private final SystemMessageProperties systemMessageProperties = new SystemMessageProperties();
    @Autowired
    private final EndpointsProperties endpointsProperties = new EndpointsProperties();

    @BeforeEach
    void setUp() {
        MatrixRequest bareMatrixRequest = new MatrixRequest(new ArrayList<>());
        bareMatrixResponse = new MatrixResponse(new MatrixResult(null, null), bareMatrixRequest, systemMessageProperties, endpointsProperties);
    }

    @Test
    void getResponseInformation() {

        assertEquals(MatrixResponseInfo.class, bareMatrixResponse.responseInformation.getClass());
        assertNotNull(bareMatrixResponse.responseInformation);
    }
}
