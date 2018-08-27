package heigit.ors.controllersOld;

import heigit.ors.controllersOld.converters2.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//@EnableWebMvc
//@Configuration
public class WebConfig  implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        Collection<HttpMessageConverter<?>> customConverters = new ArrayList<>();
        customConverters.add(new RouteResponseConverter());
        customConverters.add(new RouteToGPXMessageConverter());
        customConverters.add(new RouteToJSONMessageConverter());
        converters.addAll(0, customConverters);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToCoordinateConverter());
        registry.addConverter(new StringToCoordinateSequenceWrapperConverter());
    }
}
