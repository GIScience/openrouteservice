package org.heigit.ors.config.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DynamicDataProfileProperties {
    @JsonProperty("datasets")
    private List<String> datasets = new ArrayList<>();

    public DynamicDataProfileProperties() {
    }

    public DynamicDataProfileProperties(String ignored) {
        // This constructor is used to create an empty object for the purpose of ignoring it in the JSON serialization.
    }

    @JsonIgnore
    public boolean isEmpty() {
        return datasets == null || datasets.isEmpty();
    }

    public void merge(DynamicDataProfileProperties other) {
        if (other != null && other.getDatasets() != null && !other.getDatasets().isEmpty()) {
            if (this.datasets == null) {
                this.datasets = new ArrayList<>();
            }
            for (String ds : other.getDatasets()) {
                if (!this.datasets.contains(ds)) {
                    this.datasets.add(ds);
                }
            }
        }
    }

    public List<String> getEnabledDynamicDatasets() {
        return datasets != null ? new ArrayList<>(datasets) : new ArrayList<>();
    }
}


