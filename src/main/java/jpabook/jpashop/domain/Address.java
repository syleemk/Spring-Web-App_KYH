package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

//값 타입은 변경이 되어선 안됨 Immutable하게 설계되어야함
@Getter
@Embeddable
public class Address {
    private String city;
    private String street;
    private String zipcode;

    //jpa 기본 스펙이
    //이런 객체들은 jpa가 생성할 때, 리플렉션이나 프록시 같은 기술을 써야될 때가 굉장히 많음
    //기본생성자가 있어야 그게 가능
    //대신 public으로 하면 사람들이 많이 호출할 수 있으니까
    //기본스펙에서는 protected까지 허용
    //사실 이런거 상속할일이없으니까 ㅋㅋ
    protected Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
