package org.motechproject.ananya.utils;

import org.motechproject.web.message.converters.CustomJaxb2RootElementHttpMessageConverter;
import org.motechproject.web.message.converters.CustomMappingJacksonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.test.web.server.setup.StandaloneMockMvcBuilder;

public class MVCTestUtils {

    public static MockMvc mockMvc(Object controller) {
        StandaloneMockMvcBuilder mockMvcBuilder = MockMvcBuilders.standaloneSetup(controller);
        mockMvcBuilder.setMessageConverters(new CustomMappingJacksonHttpMessageConverter(), new CustomJaxb2RootElementHttpMessageConverter());
        return mockMvcBuilder.build();
    }
}

