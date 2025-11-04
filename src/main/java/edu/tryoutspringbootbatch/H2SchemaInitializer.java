package edu.tryoutspringbootbatch;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Forces Spring Batch schema creation on startup for H2.
 */
@Component
public class H2SchemaInitializer {

  @Autowired
  private DataSource dataSource;

  @PostConstruct
  public void initializeSchema() {
    System.out.println("⚙️ Initializing Spring Batch schema manually...");
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
        new ClassPathResource("org/springframework/batch/core/schema-h2.sql")
    );
    populator.execute(dataSource);
    System.out.println("✅ Spring Batch metadata tables created successfully!");
  }
}
