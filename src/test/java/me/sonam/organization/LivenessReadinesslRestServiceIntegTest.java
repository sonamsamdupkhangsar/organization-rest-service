package me.sonam.organization;

import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Test the liveness and readiness endpoints
 */
@AutoConfigureWebTestClient
@Log
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
public class LivenessReadinesslRestServiceIntegTest {
  private static final Logger LOG = LoggerFactory.getLogger(LivenessReadinesslRestServiceIntegTest.class);

  @Autowired
  private WebTestClient client;

  @MockBean
  private ReactiveJwtDecoder reactiveJwtDecoder;
  @Test
  public void readiness() {
    LOG.info("check readiness endpoint");
    client.get().uri("/organizations/api/health/readiness")
            .exchange().expectStatus().isOk();
  }

  @Test
  public void liveness() {
    LOG.info("check liveness endpoint");
    client.get().uri("/organizations/api/health/liveness")
            .exchange().expectStatus().isOk();
  }
}
