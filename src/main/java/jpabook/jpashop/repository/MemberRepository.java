package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    //메서드 이름보고 spring data jpa가 jpql생성해서 구현해줌
    //Name이라는 키워드보고 jpql 아래와 같이 만듦
    //select m from Member m where m.name = ?
    List<Member> findByName(String name);
}
