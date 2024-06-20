package cn.limbo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RespValidate {

  @JsonProperty("isValid")
  private boolean isValid;

  public RespValidate() {
  }

  public RespValidate fromJsonString(String json) throws JsonProcessingException {
    var mapper = new ObjectMapper();
    return mapper.readValue(json, RespValidate.class);
  }

  public boolean isValid() {
    return isValid;
  }

  public void setValid(boolean valid) {
    isValid = valid;
  }
}
