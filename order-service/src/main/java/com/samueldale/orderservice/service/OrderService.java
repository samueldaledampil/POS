package com.samueldale.orderservice.service;

import com.samueldale.orderservice.dto.OrderLineItemsDto;
import com.samueldale.orderservice.dto.OrderRequest;
import com.samueldale.orderservice.model.Order;
import com.samueldale.orderservice.model.OrderLineItems;
import com.samueldale.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        //Call inventory service to check if product is in stock
        Boolean isInStock = webClient.get()
                .uri("https://localhost:8082/api/inventory")
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if(isInStock) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Product is out of stock. Please try again later");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {

        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItems.getPrice());
        orderLineItems.setSkuCode(orderLineItems.getSkuCode());
        orderLineItems.setQuantity(orderLineItems.getQuantity());

        return orderLineItems;
    }

}
