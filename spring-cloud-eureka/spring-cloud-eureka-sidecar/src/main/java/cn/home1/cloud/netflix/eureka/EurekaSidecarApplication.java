package cn.home1.cloud.netflix.eureka;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.cloud.netflix.sidecar.EnableSidecar;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

/**
 * The @EnableSidecar annotation includes @EnableCircuitBreaker, @EnableDiscoveryClient, and @EnableZuulProxy
 */
@EnableSidecar
@SpringBootApplication
@Slf4j
public class EurekaSidecarApplication {

  @Autowired
  private Environment environment;

  @Autowired
  private SecurityProperties securityProperties;

  /**
   * see: {@link org.springframework.security.provisioning.InMemoryUserDetailsManager#createUser(UserDetails)}
   */
  public void printInfo() {
    log.info("server.context-path: {}", this.environment.getProperty("server.context-path"));
    log.info("management.context-path: {}", this.environment.getProperty("management.context-path"));

    final String passwordFromSystemEnv = System.getenv("SPRING_SECURITY_USER_PASSWORD");
    if (StringUtils.isEmpty(passwordFromSystemEnv)) {
      final String username = this.securityProperties.getUser().getName();
      final String password = this.securityProperties.getUser().getPassword();

      log.info("username: {}, randomly generated password: {}", username, password);
    }
  }

  public static void main(final String... args) {
    final ConfigurableApplicationContext context = SpringApplication.run(EurekaSidecarApplication.class, args);

    final EurekaSidecarApplication application = context.getBean(EurekaSidecarApplication.class);
    application.printInfo();
  }
}
