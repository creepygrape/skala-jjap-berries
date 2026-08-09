package com.jjap.berries.product.repository;

import com.jjap.berries.product.domain.Product;
import com.jjap.berries.product.domain.ProductStatus;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
  Page<Product> findAllByChannelId(Long channelId, Pageable pageable);

  Page<Product> findAllByChannelIdAndStatus(Long channelId, ProductStatus status, Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Product p where p.id in :ids order by p.id")
  List<Product> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Product p where p.id = :id")
  Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
