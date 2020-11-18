package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

//component스캔의 대상
@Repository
public class MemberRepository {

    //JPA에서 사용하는 EntityManager 등록
    //스프링 부트가 어노테이션보고 스프링 컨테이너에서 em 주입해줌
    @PersistenceContext
    //스프링 데이터 jpa 라이브러리 쓰면 위 어노테이션을 그냥 autowired로 바꿀 수 있음
    //따라서 그냥 requiredArgsConstructor로 대체 가능
    private final EntityManager em;

    public MemberRepository(EntityManager em) {
        this.em = em;
    }

    /**
     * 엔티티메니저를 통한 모든 데이터 변경은
     * 항상 트랜잭션 안에서 이루어져야함
     */

/*

    public Long save(Member member){
        em.persist(member);

        //왜 멤버반환 안하고, id반환?
        //커맨드랑 쿼리를 분리해라
        //저장을 하고나면, 이것은 사이드 이펙트를 일으킬 커멘드 성이기 때문에 웬만하면 리턴값 안만듦
        //대신 id정도는 있으면, 다시 조회할 수 있으니 리턴
        return member.getId();
    }
*/

    public void save(Member member){
        //영속성 컨텍스트에 member객체를 넣음
        //트랜잭션 커밋되는 시점에 insert 쿼리 날라감
        em.persist(member);
    }

    public Member findOne(Long id) {
        //단건조회
        return em.find(Member.class, id);
    }

    public List<Member> findAll(){
        //jpql사용
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name){
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
