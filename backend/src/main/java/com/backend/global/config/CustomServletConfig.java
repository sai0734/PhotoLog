package com.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.backend.global.controller.formatter.LocalDateFormatter;

@Configuration
public class CustomServletConfig implements WebMvcConfigurer{

  @Override
  public void addFormatters(FormatterRegistry registry) {
    
    registry.addFormatter(new LocalDateFormatter());
  }
      // 시큐리티로 옮김 - 진입과정에서 코스 에러가 뜨기 때문에
  //   @Override
  // public void addCorsMappings(CorsRegistry registry) {

  //   registry.addMapping("/**")
  //           .allowedOrigins("*")
  //           .allowedMethods("HEAD", "GET", "POST", "PUT", "DELETE", "OPTIONS")
  //           .maxAge(300)
  //           .allowedHeaders("Authorization", "Cache-Control", "Content-Type");
  // }
}