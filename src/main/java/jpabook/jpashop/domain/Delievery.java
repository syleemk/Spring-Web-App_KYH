package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Delievery {

    @Id @GeneratedValue
    @Column(name = "delievery_id")
    private Long id;

    private Address address;

    @OneToOne(mappedBy = "delievery")
    private Order order;

    @Enumerated(EnumType.STRING)
    private DelieveryStatus status;
}
