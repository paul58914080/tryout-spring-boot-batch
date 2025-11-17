package edu.tryoutspringbootbatch.reader;

import edu.tryoutspringbootbatch.model.Product;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class ProductReader implements ItemReader<Product>, ItemStream {

  private final FlatFileItemReader<Product> reader;

  public ProductReader() {
    this.reader = new FlatFileItemReader<>();
    reader.setLinesToSkip(1);
    reader.setResource(new ClassPathResource("/data/products/products.csv"));

    var lineTokenizer = new DelimitedLineTokenizer();
    lineTokenizer.setDelimiter(",");
    lineTokenizer.setNames("name", "description", "price");

    var lineMapper = new DefaultLineMapper<Product>();
    lineMapper.setLineTokenizer(lineTokenizer);
    lineMapper.setFieldSetMapper(fieldSet -> Product.builder()
        .name(fieldSet.readString("name"))
        .description(fieldSet.readString("description"))
        .price(fieldSet.readDouble("price"))
        .build());

    reader.setLineMapper(lineMapper);
  }

  @Override
  public Product read() throws Exception {
    return reader.read();
  }

  @Override
  public void open(ExecutionContext executionContext) {
    reader.open(executionContext);
  }

  @Override
  public void update(ExecutionContext executionContext) {
    reader.update(executionContext);
  }

  @Override
  public void close() {
    reader.close();
  }
}
