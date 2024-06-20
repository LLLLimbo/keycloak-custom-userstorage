package cn.limbo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomUser {

  @JsonProperty("id")
  private String id;
  @JsonProperty("userName")
  private String userName;
  @JsonProperty("createdAt")
  private long createdAt;
  @JsonProperty("email")
  private String email;

  public CustomUser() {
  }

  @Override
  public String toString() {
    return "CustomUser{" +
        "createdAt=" + createdAt +
        ", id='" + id + '\'' +
        ", userName='" + userName + '\'' +
        ", email='" + email + '\'' +
        '}';
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(long createdAt) {
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
