package edu.tryoutspringbootbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class TryoutSpringBootBatchApplication {

  public static void main(String[] args) {
    SpringApplication.run(TryoutSpringBootBatchApplication.class, args);
  }

  @Bean
  public CommandLineRunner checkJobs(ApplicationContext context) {
    return args -> System.out.println("Registered Jobs: " + context.getBeansOfType(Job.class).keySet());
  }

  @Bean
  public CommandLineRunner runJob(JobLauncher jobLauncher, Job simpleJob) {
    return args -> {
      System.out.println("▶️ Launching batch job manually...");
      jobLauncher.run(simpleJob, new JobParameters());
    };
  }
}
