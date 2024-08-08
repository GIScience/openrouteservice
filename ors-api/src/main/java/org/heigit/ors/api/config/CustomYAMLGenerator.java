package org.heigit.ors.api.config;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.util.StringQuotingChecker;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.io.Writer;

public class CustomYAMLGenerator extends YAMLGenerator {

    public CustomYAMLGenerator(IOContext ctxt, int jsonFeatures, int yamlFeatures, StringQuotingChecker quotingChecker, ObjectCodec codec, Writer out, DumperOptions.Version version) throws IOException {
        super(ctxt, jsonFeatures, yamlFeatures, quotingChecker, codec, out, version);
    }

    public void writeRawString(String text) throws IOException {
        this._verifyValueWrite("write String value");
        this._writeScalar(text, "string", DumperOptions.ScalarStyle.PLAIN);
    }
}
