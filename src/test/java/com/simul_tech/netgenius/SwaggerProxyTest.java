package com.simul_tech.netgenius;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(properties = {
        "springdoc.api-docs.path=/api-docs",
        "server.forward-headers-strategy=framework",
        "app.public-api-url="
})
@AutoConfigureMockMvc
class SwaggerProxyTest {
    @Autowired private MockMvc mvc;

    @Test
    void directAccessUsesTheActualHostAndPort() throws Exception {
        mvc.perform(get("http://direct.example.test:8080/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servers[0].url").value("http://direct.example.test:8080"));
    }

    @Test
    void openApiUsesExternalHttpsHostAndPrefix() throws Exception {
        mvc.perform(proxied("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servers[0].url").value("https://api.example.test/api"));
    }

    @Test
    void swaggerConfigKeepsPrefixForSchemaAndAuthorizationCallback() throws Exception {
        mvc.perform(proxied("/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/api/api-docs"))
                .andExpect(jsonPath("$.configUrl").value("/api/api-docs/swagger-config"))
                .andExpect(jsonPath("$.oauth2RedirectUrl")
                        .value("https://api.example.test/api/swagger-ui/oauth2-redirect.html"));
        mvc.perform(proxied("/swagger-ui/swagger-initializer.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/api/api-docs/swagger-config")));
    }

    @Test
    void swaggerRedirectKeepsPrefixAndPingRemainsPublic() throws Exception {
        mvc.perform(proxied("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/swagger-ui/index.html"));
        mvc.perform(proxied("/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    private MockHttpServletRequestBuilder proxied(String path) {
        return get(path)
                .header("X-Forwarded-Host", "api.example.test")
                .header("X-Forwarded-Proto", "https")
                .header("X-Forwarded-Port", "443")
                .header("X-Forwarded-Prefix", "/api");
    }
}
