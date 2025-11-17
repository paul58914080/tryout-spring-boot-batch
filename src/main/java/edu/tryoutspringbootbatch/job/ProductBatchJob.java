package edu.tryoutspringbootbatch.job;

import edu.tryoutspringbootbatch.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductBatchJob {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final ItemReader<Product> reader;
  private final ItemWriter<Product> writer;

  @Bean
  public Step productStep() {
    return new StepBuilder("productStep", jobRepository)
        .<Product, Product>chunk(10, transactionManager)
        .reader(reader)
        .writer(writer)
        .build();
  }

  @Bean("productJob")
  public Job productJob(Step productStep) {
    return new JobBuilder("productJob", jobRepository)
        .start(productStep)
        .build();
  }
}
