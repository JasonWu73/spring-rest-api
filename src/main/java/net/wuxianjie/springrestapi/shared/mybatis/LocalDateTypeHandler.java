package net.wuxianjie.springrestapi.shared.mybatis;

import cn.hutool.core.date.LocalDateTimeUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 全局配置 (application.yml):
 *
 * <p>{@code mybatis.type-handlers-package: net.wuxianjie.springrestapi.shared.mybatis}
 */
public class LocalDateTypeHandler extends BaseTypeHandler<LocalDate> {

  @Override
  public void setNonNullParameter(
    final PreparedStatement ps,
    final int i,
    final LocalDate param,
    final JdbcType jdbcType
  ) throws SQLException {
    ps.setString(i, LocalDateTimeUtil.formatNormal(param));
  }

  @Override
  public LocalDate getNullableResult(final ResultSet rs, final String columnName) throws SQLException {
    return toNullableLocalDate(rs.getString(columnName));
  }

  @Override
  public LocalDate getNullableResult(final ResultSet rs, final int columnIndex) throws SQLException {
    return toNullableLocalDate(rs.getString(columnIndex));
  }

  @Override
  public LocalDate getNullableResult(final CallableStatement cs, final int columnIndex) throws SQLException {
    return toNullableLocalDate(cs.getString(columnIndex));
  }

  private static LocalDate toNullableLocalDate(final String dateStr) {
    if (dateStr == null) {
      return null;
    }
    return LocalDateTimeUtil.parseDate(dateStr, DateTimeFormatter.ISO_DATE);
  }
}
