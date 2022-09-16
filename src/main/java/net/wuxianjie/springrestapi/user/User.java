package net.wuxianjie.springrestapi.user;

import lombok.Data;
import net.wuxianjie.springrestapi.role.Role;

import java.time.LocalDateTime;

@Data
public class User {

  private Integer id;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String remark;
  private String username;
  private String nickname;
  private String hashed_password;
  private Boolean enabled;

  /**
   * {@link Role#getId()}
   */
  private Integer roleId;
}
