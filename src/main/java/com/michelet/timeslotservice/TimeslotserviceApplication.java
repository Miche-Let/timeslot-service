package com.michelet.timeslotservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;


/**
 * 타임슬롯 서비스 애플리케이션의 실행을 담당하는 메인 클래스.
 */
@SpringBootApplication
@EnableRetry
public class TimeslotserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeslotserviceApplication.class, args);
	}

}
