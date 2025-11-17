package edu.tryoutspringbootbatch;

import edu.tryoutspringbootbatch.entity.ProductEntity;
import edu.tryoutspringbootbatch.repository.ProductRepository;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.util.List;
import javax.sql.DataSource;
import net.lbruun.springboot.preliquibase.PreLiquibaseAutoConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
public class ProductBatchTest {

  private static EmbeddedPostgres embeddedPostgres; // retain reference to stop later

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JobLauncher jobLauncher;

  @Autowired
  @Qualifier("productJob")
  private Job productJob;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private org.springframework.batch.core.repository.JobRepository jobRepository;

  @Autowired(required = false)
  private JobLauncherTestUtils jobLauncherTestUtils;

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
  @BeforeEach
  void setUp() {
    // Clear products before each test to ensure clean state
    productRepository.deleteAll();
  }

  @Test
  void shouldRunProductJobSuccessfully() throws Exception {
    // Given
    jobLauncherTestUtils.setJob(productJob);

    // When
    JobExecution jobExecution = jobLauncherTestUtils.launchJob();

    // Then
    Assertions.assertThat(jobExecution.getStatus())
        .as("Job should complete successfully")
        .isEqualTo(BatchStatus.COMPLETED);

    Assertions.assertThat(jobExecution.getExitStatus().getExitCode())
        .as("Job exit code should be COMPLETED")
        .isEqualTo("COMPLETED");
  }

  @Test
  void shouldSaveProductsFromCsvToDatabase() throws Exception {
    // Given
    jobLauncherTestUtils.setJob(productJob);
    long initialCount = productRepository.count();
    Assertions.assertThat(initialCount).isEqualTo(0L);

    // When
    JobExecution jobExecution = jobLauncherTestUtils.launchJob();

    // Then
    Assertions.assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // Verify products are saved
    List<ProductEntity> products = productRepository.findAll();
    Assertions.assertThat(products).hasSize(2)
        .as("Should have saved 2 products from CSV file");
  }

  @Test
  void shouldSaveProductsWithCorrectData() throws Exception {
    // Given
    jobLauncherTestUtils.setJob(productJob);

    // When
    JobExecution jobExecution = jobLauncherTestUtils.launchJob();

    // Then
    Assertions.assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    List<ProductEntity> products = productRepository.findAll();

    // Verify first product (Laptop)
    ProductEntity laptop = products.stream()
        .filter(p -> "Laptop".equals(p.getName()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Laptop product not found"));

    Assertions.assertThat(laptop.getName()).isEqualTo("Laptop");
    Assertions.assertThat(laptop.getDescription()).isEqualTo("Thin and light");
    Assertions.assertThat(laptop.getPrice()).isEqualTo(55000.0);
    Assertions.assertThat(laptop.getId()).isNotNull();

    // Verify second product (Mouse)
    ProductEntity mouse = products.stream()
        .filter(p -> "Mouse".equals(p.getName()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Mouse product not found"));

    Assertions.assertThat(mouse.getName()).isEqualTo("Mouse");
    Assertions.assertThat(mouse.getDescription()).isEqualTo("Wireless mouse");
    Assertions.assertThat(mouse.getPrice()).isEqualTo(1200.0);
    Assertions.assertThat(mouse.getId()).isNotNull();
  }

  @Test
  void shouldRunJobMultipleTimesWithDifferentParameters() throws Exception {
    // Given - First job execution
    JobParameters firstJobParams = new JobParametersBuilder()
        .addLong("run", 1L)
        .toJobParameters();

    // When - First execution
    JobExecution firstExecution = jobLauncher.run(productJob, firstJobParams);

    // Then
    Assertions.assertThat(firstExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    long countAfterFirstRun = productRepository.count();
    Assertions.assertThat(countAfterFirstRun).isEqualTo(2L);

    // Given - Second job execution with different parameters
    JobParameters secondJobParams = new JobParametersBuilder()
        .addLong("run", 2L)
        .toJobParameters();

    // When - Second execution
    JobExecution secondExecution = jobLauncher.run(productJob, secondJobParams);

    // Then
    Assertions.assertThat(secondExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    long countAfterSecondRun = productRepository.count();
    Assertions.assertThat(countAfterSecondRun).isEqualTo(4L)
        .as("Should have 4 products after running job twice");
  }

  @Test
  void shouldProcessCorrectNumberOfItemsInStep() throws Exception {
    // Given
    jobLauncherTestUtils.setJob(productJob);

    // When
    JobExecution jobExecution = jobLauncherTestUtils.launchJob();

    // Then
    Assertions.assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // Verify step execution metrics
    jobExecution.getStepExecutions().forEach(stepExecution -> {
      Assertions.assertThat(stepExecution.getStepName()).isEqualTo("productStep");
      Assertions.assertThat(stepExecution.getReadCount()).isEqualTo(2)
          .as("Step should read 2 items from CSV");
      Assertions.assertThat(stepExecution.getWriteCount()).isEqualTo(2)
          .as("Step should write 2 items to database");
      Assertions.assertThat(stepExecution.getCommitCount()).isGreaterThan(0)
          .as("Step should have at least one commit");
      Assertions.assertThat(stepExecution.getFilterCount()).isEqualTo(0)
          .as("Step should not filter any items");
    });
  }
}
