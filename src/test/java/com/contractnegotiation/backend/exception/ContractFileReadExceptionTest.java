package com.contractnegotiation.backend.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContractFileReadExceptionTest {

    @Test
    void messageOnlyConstructor_setsMessageAndNoCause() {
        ContractFileReadException exception = new ContractFileReadException("failed to read file");

        assertThat(exception.getMessage()).isEqualTo("failed to read file");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void messageAndCauseConstructor_setsBothFields() {
        Throwable cause = new java.io.IOException("disk error");

        ContractFileReadException exception = new ContractFileReadException("failed to read file", cause);

        assertThat(exception.getMessage()).isEqualTo("failed to read file");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void isRuntimeException() {
        ContractFileReadException exception = new ContractFileReadException("boom");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
