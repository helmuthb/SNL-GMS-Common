package gms.core.waveformqc.waveformqccontrol.util;

public class StandardResponse {

  private StatusResponse response;
  private String message;

  public StandardResponse(StatusResponse response) {
    this.response = response;
  }

  public StandardResponse(StatusResponse response, String message) {
    this.response = response;
    this.message = message;
  }

  public StatusResponse getResponse() {
    return response;
  }

  public void setResponse(StatusResponse response) {
    this.response = response;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
