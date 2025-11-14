package edu.tryoutspringbootbatch.resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class JobLaunchController {

  private final JobLauncher jobLauncher;
  private final Job job;

  @GetMapping("/_launchjob/{id}")
  public String launchJob(@PathVariable("id")String id) throws Exception {
    log.info("Starting job...");
    var jobParameters = new JobParametersBuilder()
        .addString("jobId", id)
        .toJobParameters();
    jobLauncher.run(job, jobParameters);
    log.info("Job started.");
    return "Job launched!";
  }
}
