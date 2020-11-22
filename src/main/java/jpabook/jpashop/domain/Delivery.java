package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    private Address address;

    @OneToOne(mappedBy = "delivery")
    private Order order;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
}
