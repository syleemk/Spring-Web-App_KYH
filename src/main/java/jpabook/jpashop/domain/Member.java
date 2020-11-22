package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Member {
    //generatedValue 전략 쓰면
    //em.persist()할때 insert쿼리 날려서 id값을 영속성 컨텍스트 안에 넣어놓음
    //id값은 항상 있는 것을 보장함
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    @JsonIgnore
    @OneToMany(mappedBy = "member")
    List<Order> orders = new ArrayList<>();

}
