package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringRunner.class) //단위 테스트 아닌, 스프링 다 띄워서 하는 테스트
@SpringBootTest
@Transactional //테스트 코드에 있으면 db반영안되고 롤백됨
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;

    @Test
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long savedId = memberService.join(member);

        //then
        /**
         * 이게 가능한 이유는 Transactional어노테이션 덕분
         * JPA에서 같은 트랜잭션에 안에선 같은 영속성 컨텍스트 안에서 관리가 됨
         */
        Assertions.assertThat(member).isEqualTo(memberService.findOne(savedId));
    }
    
    @Test(expected = IllegalStateException.class) //던져진 예외가 이거면 테스트 통과
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");
        
        //when
        memberService.join(member1);
        memberService.join(member2); // 여기서 예외 발생해야함
/*
        //너무 지저분하니까 어노테이션 지원함
        try{
            memberService.join(member2); // 여기서 예외 발생해야함
        } catch (IllegalStateException e){
            return;
        }
*/

        //아래 로직이 실행되면 fail임
        
        //then
        Assertions.fail("예외가 발생해야 한다.");
    }
}