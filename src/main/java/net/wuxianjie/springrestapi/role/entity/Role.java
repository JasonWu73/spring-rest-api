package net.wuxianjie.springrestapi.role.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Role {

  private Integer id;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String remark;
  private String name;
  private String menus;
}
