package net.wuxianjie.springrestapi.role.entity;

import lombok.Data;

@Data
public class RoleMenu {

  private Integer id;

  /**
   * {@link Role#getId()}.
   */
  private Integer roleId;

  /**
   * {@link Menu#getId()}.
   */
  private Integer menuId;
}
