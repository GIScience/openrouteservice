---
parent: Contributing
nav_order: 1
title: Contributing Translations
---

# Translations
If you notice any thing wrong with translations, or you want to add a new language to the openrouteservice instructions, please add/edit files in the [`src/main/resources/resources/locales`](https://github.com/GIScience/openrouteservice/tree/master/openrouteservice/src/main/resources/resources/locales) folder. You can use the [`ors_en.resources`](https://github.com/GIScience/openrouteservice/openrouteservice/src/main/resources/resources/locales/ors_en-US.resources) file as a template as to what information needs to be present.
As a guide, for each instruction there are two formats - one where there is a named place (i.e. 47 Berlinerstraße) and one without. It is important to keep the `{way_name}` tag in the text, but it should be moved to the correct location.
To show openrouteservice what language is being used, you should alter the name of the file to include the ISO identifier (i.e. for Austrian German the filename would be `ors_de_AT.resources`).

Since the update to the v2 API, we make use of the Spring framework. Because of that, you also need to make a slight modification to a java file that declares what parameters can be passed through to the API. This file is located in [`openrouteservice/src/main/java/heigit/ors/api/requests/common/APIEnums.java`](https://github.com/GIScience/openrouteservice/blob/master/openrouteservice/src/main/java/org/heigit/ors/api/requests/common/APIEnums.java) and the section that needs updating is the `public enum Languages` enum. If (for example) you want to add Polish to the languages, you would also need to add here something like `PL("pl")` to the list where the part in the `("...")` is what would be passed in to the API to select that language. So ultimately you would end up with:
```java
public enum Languages {
        DE("de"),
        EN("en"),
        EN_US("en-us"),
        ES("es"),
        FR("fr"),
        GR("gr"),
        HU("hu"),
        IT("it"),
        NE("ne"),
        NL("nl"),
        PL("pl"),
        PT("pt"),
        RU("ru"),
        ZH_CN("zh-cn");
```
