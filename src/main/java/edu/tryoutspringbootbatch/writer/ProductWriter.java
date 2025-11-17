package edu.tryoutspringbootbatch.writer;

import edu.tryoutspringbootbatch.entity.ProductEntity;
import edu.tryoutspringbootbatch.model.Product;
import edu.tryoutspringbootbatch.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductWriter implements ItemWriter<Product> {

  private final ProductRepository productRepository;

  public ProductWriter(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public void write(Chunk<? extends Product> chunk) throws Exception {
    var items = chunk.getItems();
    log.debug("Writing product data: {}", items);
    var productsChunksToSave = items.stream().map(product -> ProductEntity.builder()
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .build())
        .toList();
    var savedData = productRepository.saveAll(productsChunksToSave);
    log.debug("saved data: {} ", savedData);
  }
}
