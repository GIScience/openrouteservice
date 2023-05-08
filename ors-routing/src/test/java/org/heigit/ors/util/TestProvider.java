package org.heigit.ors.util;

import org.heigit.ors.api.requests.common.APIEnums;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

/**
 * IntelliJ can't resolve class usage in @MethodSource usage for parametrized tests in Junit5.
 * The usage is as followed:
 * '@ParameterizedTest'
 * '@MethodSource("org.heigit.ors.util.TestProvider#languagesEnumTestProvider")'
 */
@SuppressWarnings("unused")
public class TestProvider {

    public static Stream<Arguments> languagesEnumTestProvider() {
        return Stream.of(
                Arguments.of("de", APIEnums.Languages.DE),
                Arguments.of("de-de", APIEnums.Languages.DE_DE),
                Arguments.of("en", APIEnums.Languages.EN),
                Arguments.of("en-us", APIEnums.Languages.EN_US),
                Arguments.of("es", APIEnums.Languages.ES),
                Arguments.of("es-es", APIEnums.Languages.ES_ES),
                Arguments.of("fr", APIEnums.Languages.FR),
                Arguments.of("fr-fr", APIEnums.Languages.FR_FR),
                Arguments.of("gr", APIEnums.Languages.GR),
                Arguments.of("gr-gr", APIEnums.Languages.GR_GR),
                Arguments.of("he", APIEnums.Languages.HE),
                Arguments.of("he-il", APIEnums.Languages.HE_IL),
                Arguments.of("hu", APIEnums.Languages.HU),
                Arguments.of("hu-hu", APIEnums.Languages.HU_HU),
                Arguments.of("id", APIEnums.Languages.ID),
                Arguments.of("id-id", APIEnums.Languages.ID_ID),
                Arguments.of("it", APIEnums.Languages.IT),
                Arguments.of("it-it", APIEnums.Languages.IT_IT),
                Arguments.of("ne", APIEnums.Languages.NE),
                Arguments.of("ne-np", APIEnums.Languages.NE_NP),
                Arguments.of("nl", APIEnums.Languages.NL),
                Arguments.of("nl-nl", APIEnums.Languages.NL_NL),
                Arguments.of("pl", APIEnums.Languages.PL),
                Arguments.of("pl-pl", APIEnums.Languages.PL_PL),
                Arguments.of("pt", APIEnums.Languages.PT),
                Arguments.of("pt-pt", APIEnums.Languages.PT_PT),
                Arguments.of("ru", APIEnums.Languages.RU),
                Arguments.of("ru-ru", APIEnums.Languages.RU_RU),
                Arguments.of("zh", APIEnums.Languages.ZH),
                Arguments.of("zh-cn", APIEnums.Languages.ZH_CN)
        );
    }
}
