package net.wuxianjie.springrestapi.shared.mybatis;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 全局配置（application.yml）：
 *
 * <p>{@code mybatis.type-handlers-package: net.wuxianjie.springrestapi.shared.mybatis}
 */
@NoArgsConstructor
@AllArgsConstructor
public class EnumTypeHandler<E extends Enum<?> & EnumType> extends BaseTypeHandler<EnumType> {

  private Class<E> enumType;

  @Override
  public void setNonNullParameter(
    final PreparedStatement ps,
    final int i,
    final EnumType parameter,
    final JdbcType jdbcType
  ) throws SQLException {
    ps.setInt(i, parameter.getCode());
  }

  @Override
  public EnumType getNullableResult(final ResultSet rs, final String columnName) throws SQLException {
    return toNullableEnum(enumType, rs.getInt(columnName));
  }

  @Override
  public EnumType getNullableResult(final ResultSet rs, final int columnIndex) throws SQLException {
    return toNullableEnum(enumType, rs.getInt(columnIndex));
  }

  @Override
  public EnumType getNullableResult(final CallableStatement cs, final int columnIndex) throws SQLException {
    return toNullableEnum(enumType, cs.getInt(columnIndex));
  }

  private E toNullableEnum(final Class<E> enumClass, final int value) {
    final E[] enumConstants = enumClass.getEnumConstants();
    for (final E enumConstant : enumConstants) {
      if (enumConstant.getCode() == value) {
        return enumConstant;
      }
    }
    return null;
  }
}
