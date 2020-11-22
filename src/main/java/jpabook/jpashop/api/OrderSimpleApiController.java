package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne : 컬렉션이 아닌 연관관계 api
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * 리스트에 엔티티 그대로 담아서 보내면 어떤 문제있나 보자
     * 1. 양방향 연관관계를 가진 객체를 그대로 보내면 무한루프에 빠짐
     *  -> 한쪽은 @JsonIgnore해줘야함
     * 2. 지연로딩 설정되어있는 객체는 해당 엔티티 클래스를 상속한 프록시 객체로 초기화되어있음
     *  -> jackson라이브러리가 순수한 java객체가 아닌 프록시객체를 json화 하지 못함
     *  -> hibernate5 module로 해결
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기화
        }
        return all;
    }

    /**
     * api v1과 v2 모두 지연로딩으로 인해 
     * db 쿼리가 너무 많이 호출된다는 문제점 있음
     * 최악의 경우 쿼리수
     * N + 1 문제 -> 1(첫번째 쿼리) + N(그 결과로 추가시행되는 쿼리수) : 1 + 회원 N + 배송 N (1+2+2) 개의 쿼리 발생
     */
    @GetMapping("/api/v2/simple-orders")
    public List<OrderSimpleDto> ordersV2() {
        //stream api로 한줄로 줄일 수 있음
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderSimpleDto> result = orders.stream()
                .map(o -> new OrderSimpleDto(o))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 실무에서 jpa 성능문제 90%는 N+1 때문
     * fetch조인으로 대부분 해결됨
     * v2와 v3는 결과적으로 완전 동일하나
     * 발생하는 쿼리가 다름
     * 한방쿼리로 다 해결됨ㅋㅋ
     */
    @GetMapping("/api/v3/simple-orders")
    public List<OrderSimpleDto> orderV3() {
        return orderRepository.findAllWithMemberDelivery().stream()
                        .map(OrderSimpleDto::new)
                        .collect(Collectors.toList());
    }

    /**
     * v3까지는 엔티티로 조회해서
     * 컨트롤러 단에서 엔티티를 dto로 변환했음
     * v4는 변환필요없이 jpa에서 dto로 바로 조회해볼 것임
     * --------------
     * v3와 결과는 같으나
     * 쿼리 비교해보면 조회하는 컬럼 수가 더 적음
     * 딱 필요한 컬럼만 조회함
     * ---------------
     * 그렇다면 v3보다 v4가 더좋나?
     * 그건 아님
     * v4는 jpql 쿼리로 딱 필요한 컬럼만 가져온 것이어서 재사용성이 없음
     * 하지만, v3는 fetch조인으로 필요한 테이블 전체를 가져온 것이니까 재사용성 높음
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4() {
        //리포지토리는 가급적 순수한 엔티티를 조회하는데 사용
        //성능최적화 위해 쿼리를통해 DTO로 조회가 필요한 경우
        //그런 것들만 따로 분리하는 것이 좋음
        //유지보수성 좋아짐
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class OrderSimpleDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        //Dto가 엔티티를 파라미터로 받는 것은 크게 문제되지 않음
        //별로 중요하지 않은데서 중요한엔티티를 의존하는 것이기 때문
        public OrderSimpleDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //지연로딩 발생
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();//지연로딩 발생
        }
    }



}
