package net.wuxianjie.springrestapi.shared.pagination;

import lombok.Data;

import java.util.List;

@Data
public class PaginationResult<E> {

  /**
   * 页码。
   */
  private Integer pageNumber;

  /**
   * 每页显示条目个数。
   */
  private Integer pageSize;

  /**
   * 总条目数。
   */
  private Integer total;

  /**
   * 具体数据列表。
   */
  private List<E> list;
}
