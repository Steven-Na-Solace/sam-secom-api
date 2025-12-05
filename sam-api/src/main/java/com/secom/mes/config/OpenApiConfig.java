package com.secom.mes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SECOM MES Database API")
                        .version("1.0.0")
                        .description("""
                                REST API for SECOM Manufacturing Execution System Database

                                This API provides access to semiconductor manufacturing data including:
                                - Master data (Equipment, Shifts, Operators, Product Types, Features)
                                - Production data (Lots, Measurements, Quality Results)
                                - Analytics (Production summaries, Equipment health, Shift performance, Quality analytics)

                                **Database**: MariaDB 11.2
                                **Total Records**: 1,567 lots with 590 sensor measurements each
                                **Time Period**: July 2008 - September 2008

                                **Key Endpoints**:
                                - `/equipment` - Manufacturing equipment management
                                - `/lots` - Production lot tracking with filters
                                - `/measurements` - Sensor measurement data
                                - `/quality` - Quality inspection results
                                - `/analytics` - Analytics and reporting endpoints
                                """)
                        .contact(new Contact()
                                .name("SECOM MES Team")
                                .email("mes@secom.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api/v1")
                                .description("Local Development Server")));
    }
}
