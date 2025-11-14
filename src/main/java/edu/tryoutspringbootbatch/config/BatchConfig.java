package edu.tryoutspringbootbatch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

  @Bean
  public Job simpleJob(JobRepository jobRepository, Step simpleStep) {
    return new JobBuilder("simpleJob", jobRepository).start(simpleStep).build();
  }

  @Bean
  public Step simpleStep(JobRepository jobRepository,
      PlatformTransactionManager transactionManager) {
    return new StepBuilder("simpleStep", jobRepository).tasklet((contribution,
        chunkContext) -> {
      System.out.println("✅ Starting batch process...");
      System.out.println("Hello from Spring Batch!");
      System.out.println("✅ Batch process finished.");
      return RepeatStatus.FINISHED;
    }, transactionManager).build();
  }
}