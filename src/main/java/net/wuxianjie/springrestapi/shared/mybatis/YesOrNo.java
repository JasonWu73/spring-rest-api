package net.wuxianjie.springrestapi.shared.mybatis;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

@Getter
@ToString
@RequiredArgsConstructor
public enum YesOrNo implements EnumType {

  NO(0),
  YES(1);

  private static final YesOrNo[] VALUES;

  static {
    VALUES = values();
  }

  @JsonValue
  private final int code;

  public static Optional<YesOrNo> resolve(final Integer code) {
    if (code == null) {
      return Optional.empty();
    }
    for (final YesOrNo yesOrNo : VALUES) {
      if (yesOrNo.getCode() == code) {
        return Optional.of(yesOrNo);
      }
    }
    return Optional.empty();
  }
}
