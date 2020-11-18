package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

//Junit한테 스프링과 관련된 테스트한다고 알려주는 어노테이션
@RunWith(SpringRunner.class)
@SpringBootTest
@Rollback(value = false)
public class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;

    @Test
    @Transactional // 엔티티매니저를 통한 데이터변경은 트랜잭션안에서 이루어져야함
    public void testMember() throws Exception {
        //given
        Member member = new Member();
        member.setUsername("memberA");

        //when
        Long savedId = memberRepository.save(member);
        Member findMember = memberRepository.find(savedId);

        //then
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        
        //equal메서드 오버라이딩 한게 아니니까 ==비교하게됨
        //같은 트랜잭션안에서는 영속성 컨텍스트가 같음 (em에서 트랜잭션 얻어쓰는거니까)
        //같아야함, 하나의 영속성컨텍스트안에 있는 같은 객체 참조한거니까
        //같은 영속성 컨텍스트안에선 Id값이 같으면 같은 엔티티로 식별함
        Assertions.assertThat(findMember).isEqualTo(member);
    }

}