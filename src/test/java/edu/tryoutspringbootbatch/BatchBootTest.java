package edu.tryoutspringbootbatch;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import javax.sql.DataSource;
import net.lbruun.springboot.preliquibase.PreLiquibaseAutoConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class BatchBootTest {

  private static EmbeddedPostgres embeddedPostgres; // retain reference to stop later

  @Autowired
  private DataSource dataSource;

  @TestConfiguration
  @ImportAutoConfiguration(PreLiquibaseAutoConfiguration.class)
  static class TestConfig {
    @Bean
    @Primary
    public DataSource dataSource() throws Exception {
      embeddedPostgres = EmbeddedPostgres.builder().setPort(30432).start();
      return embeddedPostgres.getPostgresDatabase();
    }
  }

  @AfterAll
  static void shutdown() throws Exception {
    if (embeddedPostgres != null) {
      embeddedPostgres.close();
    }
  }

  @Test
  void liquibaseHasCreatedProductTable() throws Exception {
    try (Connection c = dataSource.getConnection()) {
      DatabaseMetaData meta = c.getMetaData();
      ResultSet rs = meta.getTables(null, null, "t_product", null);
      Assertions.assertThat(rs.next()).as("T_PRODUCT table should exist from Liquibase changelog").isTrue();
      Assertions.assertThat(meta.getURL())
          .contains("jdbc:postgresql")
          .as("Should be using embedded Postgres, not H2");
    }
  }

  @Test
  void batchTablesExist() throws Exception {
    try (Connection c = dataSource.getConnection()) {
      DatabaseMetaData meta = c.getMetaData();
      ResultSet rs = meta.getTables(null, null, "batch_job_instance", null);
      Assertions.assertThat(rs.next()).as("Spring Batch metadata table BATCH_JOB_INSTANCE should exist").isTrue();
    }
  }
}
