package net.wuxianjie.springrestapi.shared.mybatis;

import cn.hutool.core.date.LocalDateTimeUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * 全局配置（application.yml）：
 *
 * <p>{@code mybatis.type-handlers-package: net.wuxianjie.springrestapi.shared.mybatis}
 */
public class LocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

  @Override
  public void setNonNullParameter(
    final PreparedStatement ps,
    final int i,
    final LocalDateTime param,
    final JdbcType jdbcType
  ) throws SQLException {
    ps.setString(i, LocalDateTimeUtil.formatNormal(param));
  }

  @Override
  public LocalDateTime getNullableResult(final ResultSet rs, final String columnName) throws SQLException {
    return toNullableLocalDateTime(rs.getTimestamp(columnName));
  }

  @Override
  public LocalDateTime getNullableResult(final ResultSet rs, final int columnIndex) throws SQLException {
    return toNullableLocalDateTime(rs.getTimestamp(columnIndex));
  }

  @Override
  public LocalDateTime getNullableResult(final CallableStatement cs, final int columnIndex) throws SQLException {
    return toNullableLocalDateTime(cs.getTimestamp(columnIndex));
  }

  private static LocalDateTime toNullableLocalDateTime(final Timestamp timestamp) {
    if (timestamp == null) {
      return null;
    }
    return timestamp.toLocalDateTime();
  }
}
