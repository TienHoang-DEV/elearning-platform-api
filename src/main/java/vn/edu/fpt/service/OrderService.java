package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.OrderDto;
import vn.edu.fpt.entity.Order;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.OrderRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    private final OrderRepository repository;
    private final DtoMapper dtoMapper;

    public OrderService(OrderRepository orderRepository, DtoMapper dtoMapper) {
        this.repository = orderRepository;
        this.dtoMapper = dtoMapper;
    }

    public List<Order> findAll() { return repository.findAll(); }
    public Optional<Order> findById(Integer id) { return repository.findById(id); }
    public Order save(Order entity) { return repository.save(entity); }
    public void deleteById(Integer id) { repository.deleteById(id); }
    public boolean existsById(Integer id) { return repository.existsById(id); }

    public List<OrderDto> getPurchaseHistory(User user) {
        if (user == null) {
            return java.util.Collections.emptyList();
        }
        List<Order> orders = repository.findByUser(user);
        List<OrderDto> orderDtos = new java.util.ArrayList<>();
        for (Order order : orders) {
            orderDtos.add(dtoMapper.toOrderDto(order));
        }
        return orderDtos;
    }
}
