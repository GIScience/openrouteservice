package org.heigit.ors.api.converters;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PathDeserializerTest {
    private ObjectMapper mapper;

    /**
     * Helper class to test the deserialization of a Path object.
     * The class has a single field of type Path.
     * The field setFilepath is annotated with @JsonDeserialize to use the PathDeserializer.
     */
    @JsonTypeName("HelperClass")
    private static class HelperClass {

        private Path filepath = Path.of("");

        public Path getFilepath() {
            return this.filepath;
        }

        @JsonSetter("filepath")
        @JsonDeserialize(using = PathDeserializer.class)
        public void setFilepath(Path filepath) {
            this.filepath = filepath;
        }
    }

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Object.class, new PathDeserializer());
        mapper.registerModule(module);
    }

    @ParameterizedTest
    @CsvSource({"src/test/resources/test.txt, src/test/resources/test.txt", "src/test/resources/test2.txt, src/test/resources/test2.txt", "src/test/resources/test3.txt, src/test/resources/test3.txt", "'',''", ",''", "null,''"})
    void testDeserializePath(String input, String expected) throws IOException {
        HelperClass foo = mapper.readValue("{\"filepath\":\"" + input + "\"}", HelperClass.class);
        if (expected.isEmpty()) {
            assertEquals(Path.of(""), foo.getFilepath());
        } else {
            assertEquals(Path.of(expected).toAbsolutePath(), foo.getFilepath());
        }
    }
}