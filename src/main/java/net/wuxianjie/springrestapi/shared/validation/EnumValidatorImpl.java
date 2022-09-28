package net.wuxianjie.springrestapi.shared.validation;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EnumValidatorImpl implements ConstraintValidator<EnumValidator, Object> {

  private boolean isPassed = false;
  private List<Object> values;

  @Override
  public void initialize(final EnumValidator constraintAnnotation) {
    values = new ArrayList<>();
    final Class<? extends Enum<?>> enumClass = constraintAnnotation.value();
    final Enum<?>[] enumConstants = enumClass.getEnumConstants();
    if (enumConstants == null) {
      return;
    }

    for (final Enum<?> anEnum : enumConstants) {
      try {
        final Method method = anEnum.getClass().getDeclaredMethod("getCode");
        method.setAccessible(true);
        values.add(method.invoke(anEnum));
      } catch (NoSuchMethodException e) {
        log.warn("忽略枚举值校验 [{} 不存在 getCode() 方法]", enumClass.getName());
        isPassed = true;
        break;
      } catch (InvocationTargetException | IllegalAccessException e) {
        log.warn("忽略枚举值校验 [{}.getCode() 方法执行出错]", enumClass.getName());
        isPassed = true;
        break;
      }
    }
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    return isPassed || value == null || values.contains(value);
  }
}
