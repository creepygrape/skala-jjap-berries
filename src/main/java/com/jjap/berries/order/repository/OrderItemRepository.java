package com.jjap.berries.order.repository;

import com.jjap.berries.order.domain.OrderItem;
import java.util.List;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
  List<OrderItem> findAllByOrderId(Long orderId);

  @Query(
      "select oi from OrderItem oi join fetch oi.product "
          + "where oi.order.id in :orderIds order by oi.order.id, oi.id")
  List<OrderItem> findAllWithProductByOrderIdIn(@Param("orderIds") Collection<Long> orderIds);
}
