package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor //private final로 선언된 멤버변수 초기화하는 생성자
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/new")
    public String creatFrom(Model model) {
        //빈 껍데기 memberFrom()객체를 가지고감
        //validation같은 거해주니까 빈껍데기라도 들고감ㄴ
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    // spring에서 검증 어노테이션 제공해줌
    // BindingResult라는 스프링에서 제공하는 기능, 검증한 것다음에 이게 나오면
    // 오류가 result에 담겨서 코드가 실행이됨
    public String create(@Valid MemberForm form, BindingResult result) {

        if (result.hasErrors()) {
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);

        return "redirect:/";
    }

    @GetMapping("memebers")
    public String list(Model model) {
        // 여기서는 왜 DTO로 안뿌리고, 그냥 member Entity뿌림?
        // 간단하니까, 실무에서는 변환해서 뿌리는 것을 권장
        /**
         * 서버에서 템플릿 엔진으로 렌더링하는 것은 어차피, 서버측에서 필요한 데이터만 뽑아쓰니까
         * 엔티티 바로 넘겨도 상관없음
         * 하지만, API로 만들때는 이유불문하고 절대 엔티티 외부로 넘기면 안됨
         * 이유 : API라는 것은 스펙임 + 불필요한 정보 노출
         * 엔티티에 필드 하나만 추가되도 api스펙이 변해버림
         * 엔티티에 로직하나만 추가되도 api스펙이 변해버리니까 굉장히 불안정한 api스펙이되버림
         */
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }
}
