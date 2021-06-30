package io.eventuate;

public class EventuateUnknownResponseException extends EventuateException {
  private final int httpStatusCode;
  private final String errorCode;

  public EventuateUnknownResponseException(int httpStatusCode, String errorCode) {
    super("Unknown response: HttpStatusCode=" + httpStatusCode + ", errorCode=" + errorCode);
    this.httpStatusCode = httpStatusCode;
    this.errorCode = errorCode;
  }

  public int getHttpStatusCode() {
    return httpStatusCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
