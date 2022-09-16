package net.wuxianjie.springrestapi.shared.security.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @see <a href="https://stackoverflow.com/questions/41891329/java-8-spring-constants-in-preauthorize-annotation">Java 8/Spring constants in PreAuthorize annotation - Stack Overflow</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Authority {
  // view, add, edit, del

  public static class OperationLog {
    public static final String ROOT = "op_log";
    public static final String VIEW = "op_log_view";

    public static final String HAS_VIEW = "hasAuthority('op_log_view')";
  }

  public static class UserManagement {
    public static final String ROOT = "user_mgmt";
    public static final String VIEW = "user_mgmt_view";
    public static final String ADD = "user_mgmt_add";
    public static final String EDIT = "user_mgmt_edit";
    public static final String DEL = "user_mgmt_del";

    public static final String HAS_VIEW = "hasAuthority('user_mgmt_view')";
    public static final String HAS_ADD = "hasAuthority('user_mgmt_add')";
    public static final String HAS_EDIT = "hasAuthority('user_mgmt_edit')";
    public static final String HAS_DEL = "hasAuthority('user_mgmt_del')";
  }

  public static class RoleManagement {
    public static final String ROOT = "role_mgmt";
    public static final String VIEW = "role_mgmt_view";
    public static final String ADD = "role_mgmt_add";
    public static final String EDIT = "role_mgmt_edit";
    public static final String DEL = "role_mgmt_del";

    public static final String HAS_VIEW = "hasAuthority('role_mgmt_view')";
    public static final String HAS_ADD = "hasAuthority('role_mgmt_add')";
    public static final String HAS_EDIT = "hasAuthority('role_mgmt_edit')";
    public static final String HAS_DEL = "hasAuthority('role_mgmt_del')";
  }
}
