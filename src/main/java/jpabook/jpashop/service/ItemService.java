package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    /**
     * 상품 서비스는 단순하게 상품 리포지토리에 위임만 하는 클래스
     */

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item){
        itemRepository.save(item);
    }

    @Transactional
    public void updateItem(Long itemId, String name, int price) {

        //findOne메서드로 찾아온 item 엔티티는 영속상태임
        Item findItem = itemRepository.findOne(itemId);

        /**
         * 이런식으로 setter로 풀어놓는 것보다
         * 차라리 change(name, price)이런 식으로라도 메서드를 만들어놓는게 좋음
         * setter로 여러군데 풀어놓으면 어디서 변경되는지 추적하기 너무 힘듦
         * 하지만 메서드 만들면 그 메서드 (change메서드)만 추적하면 됨
         */

        findItem.setName(name);
        findItem.setPrice(price);
        
        //변경된 내용은 더티체킹되서 트랜잭션 커밋되는 시점에 update쿼리 날아감
    }

    public List<Item> findItems(){
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId){
        return itemRepository.findOne(itemId);
    }
}
