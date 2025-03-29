package edu.hawaii.its.api.type;

import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.Objects;

public class ApiError {

    private final HttpStatus status;
    private final String resultCode;
    private final LocalDateTime timestamp;
    private final String message;
    private final String path;
    private final String stackTrace;
    private final String debugMessage;

    private ApiError(Builder builder) {
        this.status = builder.status;
        this.resultCode = builder.resultCode != null ? builder.resultCode : "FAILURE";
        this.timestamp = LocalDateTime.now();
        this.message = builder.message;
        this.path = builder.path;
        this.stackTrace = builder.stackTrace;
        this.debugMessage = builder.debugMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getResultCode() {
        return resultCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String getStackTrace() {
        return stackTrace;
    }
    public String getDebugMessage() { return debugMessage; }

    public static class Builder {
        private HttpStatus status;
        private String resultCode;
        private String message;
        private String path;
        private String stackTrace;
        private String debugMessage;
        private LocalDateTime timestamp;


        public Builder status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public Builder resultCode(String resultCode) {
            this.resultCode = resultCode;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder stackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        public Builder debugMessage(String debugMessage) {
            this.debugMessage = debugMessage;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }


        public ApiError build() {
            Objects.requireNonNull(this.status, "status cannot be null");
            Objects.requireNonNull(this.message, "message cannot be null");
            Objects.requireNonNull(this.path, "path cannot be null");
            Objects.requireNonNull(this.stackTrace, "stackTrace cannot be null");
            Objects.requireNonNull(this.debugMessage, "debugMessage cannot be null");

            return new ApiError(this);
        }
    }
}
