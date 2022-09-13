package net.wuxianjie.springrestapi.shared.pagination;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PaginationOffsetAspect {

  /**
   * 匹配所有符合以下条件的方法：
   *
   * <ol>
   *     <li>类名后缀为 Controller</li>
   *     <li>方法的访问修饰符为 public</li>
   *     <li>方法的第一个参数为 {@link PaginationRequest}</li>
   *     <li>方法的返回值为 {@link PaginationResult}</li>
   * </ol>
   *
   * @param joinPoint 程序执行期间的一个点，Spring AOP 中即为一个方法执行
   */
  // execution([方法的可见性] 返回类型 [方法所在类的全路径名].方法名(参数类型列表) [方法抛出的异常类型])
  @Before("execution(public net.wuxianjie.springrestapi.shared.pagination.PaginationResult *..*Controller.*(net.wuxianjie.springrestapi.shared.pagination.PaginationRequest, ..))")
  public void beforeCallGetByPagination(JoinPoint joinPoint) {
    final Object[] args = joinPoint.getArgs();
    for (final Object arg : args) {
      if (arg instanceof PaginationRequest) {
        ((PaginationRequest) arg).setOffset();
        break;
      }
    }
  }
}
