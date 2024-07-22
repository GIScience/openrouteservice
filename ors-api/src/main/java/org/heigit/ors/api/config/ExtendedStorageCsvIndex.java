package org.heigit.ors.api.config;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * This class represents the configuration for the CSV index storage.
 * The file input is a csv file with weights.
 * The name is lower 'csv' because it is historically used in the code. Should be renamed to 'CsvIndex' in the future.
 */
@JsonTypeName("csv")
public class ExtendedStorageCsvIndex extends ExtendedStorageIndex {

    public ExtendedStorageCsvIndex() {
    }

}

