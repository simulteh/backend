package com.simul_tech.netgenius;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Схема текущей ВМ: Swagger в корне домена, запросы API через /api. */
@SpringBootTest(properties = {
        "springdoc.api-docs.path=/api-docs",
        "server.forward-headers-strategy=framework",
        "app.public-api-url=/api"
})
@AutoConfigureMockMvc
class SwaggerRootUiTest {
    @Autowired private MockMvc mvc;

    @Test
    void rootSwaggerLoadsItsConfigAndSchemaFromRootWhileApiUsesPrefix() throws Exception {
        mvc.perform(root("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("swagger-initializer.js")));
        mvc.perform(root("/swagger-ui/swagger-initializer.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api-docs/swagger-config")))
                .andExpect(content().string(not(containsString("petstore.swagger.io"))));
        mvc.perform(root("/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.url").value("/api-docs"))
                .andExpect(jsonPath("$.configUrl").value("/api-docs/swagger-config"));
        mvc.perform(root("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servers[0].url").value("/api"))
                .andExpect(jsonPath("$.paths['/auth/login']").exists());
    }

    @Test
    void rootSwaggerRedirectStaysAtRoot() throws Exception {
        mvc.perform(root("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }

    private MockHttpServletRequestBuilder root(String path) {
        return get(path)
                .header("X-Forwarded-Host", "www.simultech.ru")
                .header("X-Forwarded-Proto", "https")
                .header("X-Forwarded-Port", "443");
    }
}
