package org.sitenv.spring.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * Root Spring configuration used to bootstrap an application context for the
 * resource providers, which construct it directly via
 * {@code new AnnotationConfigApplicationContext(AppConfig.class)} rather than
 * relying on the servlet-managed context. Scans the whole {@code org.sitenv.spring}
 * package so services, DAOs, and other configuration classes are picked up.
 */
@Configuration
//@EnableWebMvc
@EnableTransactionManagement
@ComponentScan(basePackages = "org.sitenv.spring")
public class AppConfig  {

}
