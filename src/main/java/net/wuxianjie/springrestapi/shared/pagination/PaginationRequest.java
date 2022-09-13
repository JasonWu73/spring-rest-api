package net.wuxianjie.springrestapi.shared.pagination;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class PaginationRequest {

  @NotNull(message = "页码不能为 null")
  @Min(value = 1, message = "页码不能小于 1")
  private Integer pageNumber;

  @NotNull(message = "每页显示条目个数不能为 null")
  @Min(value = 1, message = "每页显示条目个数不能小于 1")
  private Integer pageSize;

  /**
   * MySQL、SQLite 等数据库的偏移量 OFFSET，例如：
   *
   * <ul>
   *   <li>{@code SELECT * FROM table_name LIMIT #{pageSize} OFFSET #{offset}}</li>
   *   <li>{@code SELECT * FROM table_name LIMIT #{offset}, #{pageSize}}</li>
   * </ul>
   */
  @Setter(AccessLevel.PRIVATE)
  private Integer offset;

  public void setOffset() {
    setOffset((pageNumber - 1) * pageSize);
  }
}
