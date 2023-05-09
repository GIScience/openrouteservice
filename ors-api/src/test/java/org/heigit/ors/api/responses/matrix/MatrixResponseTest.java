package org.heigit.ors.api.responses.matrix;

import org.heigit.ors.api.requests.matrix.MatrixRequest;
import org.heigit.ors.matrix.MatrixResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MatrixResponseTest {
    private MatrixResponse bareMatrixResponse;


    @BeforeEach
    void setUp() {
        MatrixRequest bareMatrixRequest = new MatrixRequest(new ArrayList<>());
        bareMatrixResponse = new MatrixResponse(new MatrixResult(null, null), bareMatrixRequest);
    }

    @Test
    void getResponseInformation() {

        assertEquals(MatrixResponseInfo.class, bareMatrixResponse.responseInformation.getClass());
        assertNotNull(bareMatrixResponse.responseInformation);
    }
}
