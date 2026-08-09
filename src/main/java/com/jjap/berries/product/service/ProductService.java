package com.jjap.berries.product.service;

import com.jjap.berries.channel.repository.ChannelRepository;
import com.jjap.berries.global.exception.BusinessException;
import com.jjap.berries.global.exception.ErrorCode;
import com.jjap.berries.global.service.AccessService;
import com.jjap.berries.product.domain.Product;
import com.jjap.berries.product.domain.ProductStatus;
import com.jjap.berries.product.dto.ProductCreateRequest;
import com.jjap.berries.product.dto.ProductResponse;
import com.jjap.berries.product.dto.ProductUpdateRequest;
import com.jjap.berries.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
  private final ProductRepository products;
  private final ChannelRepository channels;
  private final AccessService access;

  public Page<ProductResponse> list(Long channelId, ProductStatus status, Pageable pageable) {
    return (status == null
            ? products.findAllByChannelId(channelId, pageable)
            : products.findAllByChannelIdAndStatus(channelId, status, pageable))
        .map(ProductResponse::from);
  }

  public ProductResponse get(Long id) {
    return ProductResponse.from(product(id));
  }

  @Transactional
  public ProductResponse create(Long userId, Long channelId, ProductCreateRequest request) {
    access.manager(access.user(userId), channelId);
    var channel =
        channels
            .findById(channelId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));
    return ProductResponse.from(
        products.save(
            new Product(
                channel,
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                request.imageUrl())));
  }

  @Transactional
  public ProductResponse update(Long userId, Long id, ProductUpdateRequest request) {
    Product product = lockedProduct(id);
    access.manager(access.user(userId), product.getChannel().getId());
    product.update(
        request.name(),
        request.description(),
        request.price(),
        request.stock(),
        request.imageUrl());
    return ProductResponse.from(product);
  }

  @Transactional
  public ProductResponse status(Long userId, Long id, ProductStatus status) {
    Product product = lockedProduct(id);
    access.manager(access.user(userId), product.getChannel().getId());
    product.changeStatus(status);
    return ProductResponse.from(product);
  }

  private Product product(Long id) {
    return products
        .findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
  }

  private Product lockedProduct(Long id) {
    return products
        .findByIdForUpdate(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
  }
}
