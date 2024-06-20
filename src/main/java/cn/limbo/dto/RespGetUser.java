package cn.limbo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RespGetUser {

  @JsonProperty("customUser")
  private CustomUser customUser;

  public RespGetUser() {
  }

  public CustomUser getCustomUser() {
    return customUser;
  }

  public void setCustomUser(CustomUser userAdapter) {
    this.customUser = userAdapter;
  }

  public RespGetUser fromJsonString(String json) throws JsonProcessingException {
    var mapper = new ObjectMapper();
    return mapper.readValue(json, RespGetUser.class);
  }
}
