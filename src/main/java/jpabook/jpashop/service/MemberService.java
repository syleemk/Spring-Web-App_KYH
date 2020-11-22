package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
//jpa의 모든 데이터 변경은 트랜잭션 안에서 일어나야함
//클래스에 이 어노테이션 쓰면, public메소드들이 다 transaction걸려서 들어감
@Transactional(readOnly = true)
public class MemberService {

    //얘를 변경할 일 없으니 final로 설정해두는 것이 좋음
    //로딩시점에 주입받고 변경될 일 없음
    //또 생성자에 값 안들어오면 컴파일 시점에 체크가능하기때문에 좋음
    private final MemberRepository memberRepository;

    // 생성자가 딱 하나인 경우는 생략 가능
    // 롬복의 AllArgsContructor 어노테이션 쓰면 만들어줌
    // 더좋은건 RequiredArgsConstructor, final 붙어있는 필드만 생성자로 만들어줌
    @Autowired 
    public MemberService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }

    /**
     * 회원 가입
     */
    @Transactional
    public Long join(Member member){
        validateDuplicateMember(member); //중복회원 검증 (예외 발생)
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    //회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }

    @Transactional
    public void update(Long id, String name) {
        //update는 변경감지 사용하는 것이 좋음 (merge쓰지마)
        Member member = memberRepository.findOne(id);
        member.setName(name);
    }
}
