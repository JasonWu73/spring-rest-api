package net.wuxianjie.springrestapi.shared.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResult<E> {

  private Long pageNumber; // 页码
  private Long pageSize; // 每页显示条目个数
  private Long total; // 总条目数
  private List<E> list; // 具体数据列表
}
