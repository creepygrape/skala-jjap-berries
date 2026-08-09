package com.jjap.berries.order.service;

import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.global.service.AccessService;
import com.jjap.berries.order.domain.Order;
import com.jjap.berries.order.domain.OrderItem;
import com.jjap.berries.order.domain.OrderStatus;
import com.jjap.berries.order.dto.OrderCreateRequest;
import com.jjap.berries.order.dto.OrderItemRequest;
import com.jjap.berries.order.dto.OrderResponse;
import com.jjap.berries.order.repository.OrderItemRepository;
import com.jjap.berries.order.repository.OrderRepository;
import com.jjap.berries.product.domain.Product;
import com.jjap.berries.product.domain.ProductStatus;
import com.jjap.berries.product.repository.ProductRepository;
import com.jjap.berries.user.domain.UserRole;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
  private final OrderRepository orders;
  private final OrderItemRepository items;
  private final ProductRepository products;
  private final AccessService access;

  @Transactional
  public OrderResponse create(Long userId, OrderCreateRequest request) {
    var user = access.user(userId);
    if (user.getRole() != UserRole.USER) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    Set<Long> ids = new HashSet<>();
    for (OrderItemRequest itemRequest : request.items()) {
      if (!ids.add(itemRequest.productId())) {
        throw new BusinessException(ErrorCode.INVALID_REQUEST);
      }
    }
    Map<Long, Product> locked = new HashMap<>();
    products.findAllByIdForUpdate(ids).forEach(p -> locked.put(p.getId(), p));
    List<Product> selectedProducts = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;
    for (OrderItemRequest itemRequest : request.items()) {
      Product product = locked.get(itemRequest.productId());
      if (product == null) {
        throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
      }
      if (product.getStatus() != ProductStatus.ON_SALE) {
        throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE);
      }
      if (product.getStock() < itemRequest.quantity()) {
        throw new BusinessException(ErrorCode.OUT_OF_STOCK);
      }
      selectedProducts.add(product);
      total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
    }
    Order o = orders.save(new Order(user, total));
    List<OrderItem> saved = new ArrayList<>();
    for (int index = 0; index < selectedProducts.size(); index++) {
      Product product = selectedProducts.get(index);
      OrderItemRequest itemRequest = request.items().get(index);
      product.decreaseStock(itemRequest.quantity());
      saved.add(items.save(new OrderItem(o, product, product.getPrice(), itemRequest.quantity())));
    }
    return response(o, saved);
  }

  public List<OrderResponse> list(Long uid) {
    access.user(uid);
    List<Order> userOrders = orders.findAllByUserIdOrderByCreatedAtDesc(uid);
    if (userOrders.isEmpty()) {
      return List.of();
    }
    Map<Long, List<OrderItem>> itemsByOrderId =
        items.findAllWithProductByOrderIdIn(userOrders.stream().map(Order::getId).toList()).stream()
            .collect(Collectors.groupingBy(item -> item.getOrder().getId()));
    return userOrders.stream()
        .map(order -> response(order, itemsByOrderId.getOrDefault(order.getId(), List.of())))
        .toList();
  }

  public OrderResponse get(Long uid, Long id) {
    Order o = order(uid, id);
    return response(o, items.findAllByOrderId(id));
  }

  @Transactional
  public OrderResponse cancel(Long uid, Long id) {
    Order o = lockedOrder(uid, id);
    if (o.getStatus() != OrderStatus.PENDING)
      throw new BusinessException(ErrorCode.ORDER_NOT_CANCELABLE);
    List<OrderItem> its = items.findAllByOrderId(id);
    Map<Long, Integer> quantitiesByProduct =
        its.stream()
            .collect(
                Collectors.toMap(
                    item -> item.getProduct().getId(),
                    OrderItem::getQuantity,
                    Integer::sum));
    Map<Long, Product> lockedProducts =
        products.findAllByIdForUpdate(quantitiesByProduct.keySet()).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));
    quantitiesByProduct.forEach(
        (productId, quantity) -> lockedProducts.get(productId).restoreStock(quantity));
    o.cancel();
    return response(o, its);
  }

  private Order order(Long userId, Long id) {
    Order o =
        orders.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    if (!o.getUser().getId().equals(userId)) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    return o;
  }

  private Order lockedOrder(Long userId, Long id) {
    Order order =
        orders
            .findByIdForUpdate(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    if (!order.getUser().getId().equals(userId)) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    return order;
  }

  private OrderResponse response(Order o, List<OrderItem> its) {
    return OrderResponse.from(o, its);
  }
}
