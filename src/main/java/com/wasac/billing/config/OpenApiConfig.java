package com.wasac.billing.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WASAC/REG Utility Billing System API")
                        .version("1.0.0")
                        .description("""
                                ## Quick test flow
                                1. **POST /api/auth/register** — register customer (no roles needed)
                                2. **POST /api/auth/verify-otp** — verify email (OTP in email or server logs)
                                3. **POST /api/auth/login** — copy `data.token` from response
                                4. Click **Authorize** → enter: `Bearer <your-token>`
                                5. Test secured endpoints

                                **Admin login:** `admin@wasac.com` / `admin123`

                                **Public endpoints** (no Authorize needed): register, verify-otp, resend-otp, login
                                """)
                        .contact(new Contact().name("WASAC/REG").email("support@wasac.rw")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH,
                                new SecurityScheme()
                                        .name(BEARER_AUTH)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste JWT from POST /api/auth/login. Format: Bearer eyJhbG...")));
    }
}
