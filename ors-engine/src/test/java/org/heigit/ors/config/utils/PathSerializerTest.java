package org.heigit.ors.config.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PathSerializerTest {
    private ObjectMapper mapper;

    /**
     * Helper class to test the deserialization of a Path object.
     * The class has a single field of type Path.
     * The field setFilepath is annotated with @JsonDeserialize to use the PathDeserializer.
     */
    @JsonTypeName("HelperClass")
    private static class HelperClass {

        private Path filepath = Path.of("");

        @JsonProperty("filepath")
        @JsonSerialize(using = PathSerializer.class)
        public Path getFilepath() {
            return this.filepath;
        }

        public void setFilepath(Path filepath) {
            this.filepath = filepath;
        }
    }

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Path.class, new PathSerializer());
        mapper.registerModule(module);
    }

    @ParameterizedTest
    @CsvSource({
            "src/test/resources/test.txt, src/test/resources/test.txt",
            "src/test/resources/test2.txt, src/test/resources/test2.txt",
            "src/test/resources/test3.txt, src/test/resources/test3.txt",
            "'',''",
            ",''",
            "null,''"
    })
    void testSerializePath(String input, String expected) throws IOException {
        HelperClass foo = new HelperClass();
        if (input != null)
            foo.setFilepath(Path.of(input));
        String json = mapper.writeValueAsString(foo);
        assertEquals("{\"filepath\":\"" + expected + "\"}", json);
    }
}