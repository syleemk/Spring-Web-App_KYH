package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * 엔티티 직접 노출
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        //hibernate5 모듈을 넣어서, lazy로딩시 프록시 객체가 정상적으로 초기화된 얘들만 api로 반환됨
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            //지연로딩 강제초기화
            order.getMember().getName();
            order.getDelivery().getAddress();
            //지연로딩되는 컬렉션 강제 초기화
            List<OrderItem> orderItems = order.getOrderItems();
            //orderItem 내부의 item 초기화
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    /**
     * 엔티티를 DTO로 변환
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 페치조인으로 성능 최적화
     * 특징 : 컨트롤러단의 코드는 완벽하게 같음
     * 단지 조회할 때, 페치조인으로 조회했냐 안했냐 차이뿐 -> 이게 엄청난 것임
     * 기존 코드 수정없이 단지 옵션만 추가해서 조회하는 것으로 성능 최적화가 이루어짐
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        return orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    /**
     * xToMany 컬렉션 조회 페이징 한계돌파
     */
    @GetMapping("/api/v3.1/orders") // 오프셋은 몇번째 페이지부터 시작할지 나타냄
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        //xToOne은 그냥 fetch 조인으로 가져오면 됨
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        //지연로딩 최적화 안됨
        return orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 컬렉션을 jpa에서 dto로 직접 조회
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * 컬렉션 dto 조회
     * in절을 통해 성능 최적화
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    /**
     * 플랫 데이터 최적화
     * order, orderitem, item을 다 조인해서 flat한 데이터를
     * 한방쿼리로 가져옴
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        //OrderFlatDto가 아닌 OrderQueryDto로 api spec을 맞추고싶음
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        //데이터를 다 돌면서 직접 발라내면 됨 ㅋㅋㅋㅋ
        return flats.stream() // OrderQueryDto를 key로, OrderItemQueryDto를 value로
                //groupby로 묶어줄려면 기준이 필요, OrderQueryDto에 equalsandhashcode 어노테이션 통해 기준 생성 (객체를 비교하는 기준이 있어야 객체 자체를 기준으로 세울 수 있음)
                .collect(Collectors.groupingBy(o->new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        Collectors.mapping(o->new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), Collectors.toList())
                )).entrySet().stream()// 여기서 orderItemList 채워주는 로직
                .map(e->new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(Collectors.toList());
    }

    //java에서 properties라고 말하면 getter와 setter를 말함
    //자바 클래스의 properites가 정의되있어야 json타입으로 serialize가 됨
    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        //DTO로 반환한다는 것은 완전히 엔티티에 대한 종속성을 끊어야함을 의미
        //엔티티가 외부로 노출되서는 안됨
        //나중에 노출된 엔티티정보 수정하면 api가 완전히 못쓰게 되버리니까
        //따라서 이것도 DTO로 감싸야함
        //private List<OrderItem> orderItems;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
/*
            //프록시 초기화
            order.getOrderItems().stream().forEach(o->o.getItem().getName());
            //위 코드 없이 이렇게만 가져오면 지연로딩되서 프록시로 초기화됨, 건드려줘야함
            orderItems = order.getOrderItems();
*/
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(Collectors.toList());
        }
    }

    //DTO 내부의 엔티티까지 모두 DTO로 감싸야함
    //엔티티 종속성 완전히 끊어내야함
    @Getter
    static class OrderItemDto {
        private  String itemName; //상품명
        private int orderPrice; //주문 가격
        private int count; //주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
