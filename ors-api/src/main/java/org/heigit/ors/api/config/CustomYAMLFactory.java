package org.heigit.ors.api.config;

import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.IOException;
import java.io.Writer;

public class CustomYAMLFactory extends YAMLFactory {
    protected YAMLGenerator _createGenerator(Writer out, IOContext ctxt) throws IOException {
        return new CustomYAMLGenerator(ctxt, _generatorFeatures, _yamlGeneratorFeatures, _quotingChecker, _objectCodec, out, _version);
    }
}
