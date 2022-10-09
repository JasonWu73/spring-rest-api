package net.wuxianjie.springrestapi.role;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Role {

  private Integer id;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String remark;
  private String name;
  private String menus; // 英文逗号分隔的权限字符串
  private Integer parentId; // 上级角色 id
  private String parentName; // 上级角色名
  private String fullPath; // 节点全路径, 以英文句号分隔的角色 id 字符串
}
