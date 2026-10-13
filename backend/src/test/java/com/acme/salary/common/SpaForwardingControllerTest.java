package com.acme.salary.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SpaForwardingController.class)
class SpaForwardingControllerTest {

    @Autowired
    private MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = {"/employees", "/insights", "/insights/IN"})
    void clientSideRoutesAreServedByTheAngularApp(String path) throws Exception {
        mvc.perform(get(path)).andExpect(forwardedUrl("/index.html"));
    }
}
