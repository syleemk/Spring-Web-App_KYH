package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 특정 화면(api)에 fit한 쿼리들을 따로 분리
 * 화면과 관련된 것과 (api종속적인 것과) 핵심 비즈니스 로직을 분리
 * 관심사를 분리할 수 있다
 */
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;


    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders(); // query 1번 -> N개 데이터 가져옴

        //orderQueryDto의 컬렉션은 jpql로 바로 가져올 수 없으니
        //돌면서 직접 채워줌
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); // 쿼리 총 N번 나옴, N+1문제
            o.setOrderItems(orderItems);
        });

        return result;
    }

    //컬렉션 조회 성능 최적화
    public List<OrderQueryDto> findAllByDto_optimization() {
        //주문 가져오는 것 까지는 똑같음
        List<OrderQueryDto> result = findOrders();

        List<Long> orderIds = toOrderIds(result);

        //orderItemMap 메모리 맵에 올려둠
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);

        //맵에서 찾아서 넣음
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        //루프를 돌지않고 한방에 가져올 것임
        //위에서는 =으로 orderId를 하나씩 가져오는데, in절로 여러개를 한꺼번에 가져올 것임
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                        "from OrderItem oi " +
                        "join oi.item i " +
                        "where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        //map collection으로 변환
        //메모리 맵에 올려두고
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                //orderId를 키값으로하는 map으로 변환
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
        return orderItemMap;
    }

    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        return result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
    }

    //일대다 연관관계이기에
    //다부분 가져오는 쿼리메서드 따로 짜야함
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        "from OrderItem oi " +
                        "join oi.item i " +
                        "where oi.order.id =:orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                //jpql에서 new메서드 인자로 컬렉션 바로 넣을 수 없음
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        "from Order o " +
                        "join o.member m " +
                        "join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    public List<OrderFlatDto> findAllByDto_flat() {
        //플랫 데이터로 다 가져옴
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i", OrderFlatDto.class)
                .getResultList();
    }
}
