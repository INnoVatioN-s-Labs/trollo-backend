package com.toyproject.trollo.common.util;

import com.toyproject.trollo.common.code.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReturnMessageTest {

    @Test
    @DisplayName("data 생성자 사용 시 성공 응답이 생성된다")
    void successConstructor() {
        ReturnMessage<String> response = new ReturnMessage<>("data");

        assertThat(response.getResult()).isEqualTo("0000");
        assertThat(response.getMessage()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isEqualTo("data");
    }

    @Test
    @DisplayName("ErrorCode 생성자 사용 시 실패 응답이 생성된다")
    void errorConstructor() {
        ReturnMessage<Void> response = new ReturnMessage<>(ErrorCode.AUTHENTICATION_FAILED);

        assertThat(response.getResult()).isEqualTo("-8408");
        assertThat(response.getMessage()).isEqualTo("이메일 또는 비밀번호가 올바르지 않습니다.");
        assertThat(response.getData()).isNull();
    }
}
