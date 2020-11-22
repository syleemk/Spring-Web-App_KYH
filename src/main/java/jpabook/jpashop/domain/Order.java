package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    //영속성 전이
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    //영속성 전이
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;


    //== 연관관계 편의 메서드 ==//
    public void setMember(Member member){
        this.member = member;
        member.orders.add(this);
    }

    public void addOrderItem(OrderItem orderItem){
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery){
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //==생성 메서드==//
    /**
     * 주문 생성 굉장히 복잡함
     * 주문만 생성해서 될 것이 아니라, 복잡한 연관관계 있어서
     * 연관된 엔티티까지 같이 넣어줘야함
     * 이러한 복잡한 생성은 별도의 생성메서드가 있으면 좋음
     * 앞으로 생성하는 시점을 변경해야하면 이것만 변경하면 됨
     */
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems){
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //==비즈니스 로직 ==//
    /**
     * 주문 취소
     */
    public void cancel() {
        //비즈니스 로직에 대한 체크 로직이 엔티티 안에 있음
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송 완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);
        //루프를 돌면서 재고를 원복
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    //==조회 로직==//

    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
/*
        int totalPrice = 0;
        //자바 스트림이나 람다 이용하면 더 깔끔하게 작성가능
        //주문할 때, totalPrice메서드 생성이유
        //상품가격과 주문수량이 있기 때문에 둘이 곱한 값을 가져와야함
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
*/
        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();

    }
}
