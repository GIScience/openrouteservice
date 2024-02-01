# Translations
If you notice anything wrong with translations, or you want to add a new language to the openrouteservice instructions, please add/edit files in the [`ors-engine/src/main/resources/resources/locales`](https://github.com/GIScience/openrouteservice/tree/main/ors-engine/src/main/resources/resources/locales) folder. You can use the [`ors_en.resources`](https://github.com/GIScience/openrouteservice/blob/main/ors-engine/src/main/resources/resources/locales/ors_en-US.resources) file as a template as to what information needs to be present.
As a guide, for each instruction there are two formats - one where there is a named place (i.e. 47 Berliner Straße) and one without. It is important to keep the `{way_name}` tag in the text, but it should be moved to the correct location.
To show openrouteservice what language is being used, you should alter the name of the file to include the ISO identifier (i.e. for Austrian German the filename would be `ors_de_AT.resources`).

Since the update to the v2 API, we make use of the Spring framework. Because of that, you also need to make a slight modification to a java file that declares what parameters can be passed through to the API. This file is located in [`ors-api/src/main/java/org/heigit/ors/api/APIEnums.java`](https://github.com/GIScience/openrouteservice/blob/main/ors-api/src/main/java/org/heigit/ors/api/APIEnums.java) and the section that needs updating is the `public enum Languages` enum. If (for example) you want to add Polish to the languages, you would also need to add here something like `PL("pl")` to the list where the part in the `("...")` is what would be passed in to the API to select that language. So ultimately you would end up with:
```java
public enum Languages {
    public enum Languages {
        CS("cs"),
        CS_CZ("cs-cz"),
        DE("de"),
        DE_DE("de-de"),
        EN("en"),
        EN_US("en-us"),
        EO("eo"),
        EO_EO("eo-eo"),
        ES("es"),
        ES_ES("es-es"),
        FR("fr"),
        FR_FR("fr-fr"),
        GR("gr"),
        GR_GR("gr-gr"),
        HE("he"),
        HE_IL("he-il"),
        HU("hu"),
        HU_HU("hu-hu"),
        ID("id"),
        ID_ID("id-id"),
        IT("it"),
        IT_IT("it-it"),
        JA("ja"),
        JA_JP("ja-jp"),
        NE("ne"),
        NE_NP("ne-np"),
        NL("nl"),
        NL_NL("nl-nl"),
        PL("pl"),
        PL_PL("pl-pl"),
        PT("pt"),
        PT_PT("pt-pt"),
        RO("ro"),
        RO_RO("ro-ro"),
        RU("ru"),
        RU_RU("ru-ru"),
        TR("tr"),
        TR_TR("tr-tr"),
        ZH("zh"),
        ZH_CN("zh-cn");
```
