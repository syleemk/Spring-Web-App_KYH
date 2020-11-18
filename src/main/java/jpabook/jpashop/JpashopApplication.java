package jpabook.jpashop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//이 어노테이션과 같은 패키지와 그 하위 패키지에 있는 패키지들이 component scan대상
@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

}
