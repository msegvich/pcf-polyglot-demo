package microsec.freddysbbq.catalog;

import org.springframework.data.repository.PagingAndSortingRepository;

import microsec.freddysbbq.catalog.model.v1.Product;

public interface ProductRepository extends PagingAndSortingRepository<Product, Long> {
}