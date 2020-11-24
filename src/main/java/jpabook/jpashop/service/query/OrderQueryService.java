package jpabook.jpashop.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * 핵심 비즈니스 로직과 쿼리를 분리
 * (커맨드와 쿼리를 분리)
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {
}
