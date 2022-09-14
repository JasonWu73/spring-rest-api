package net.wuxianjie.springrestapi.shared.validation;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.springrestapi.shared.validation.group.SaveOne;
import net.wuxianjie.springrestapi.shared.validation.group.UpdateOne;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class ValidationTestController {

  private final ObjectMapper objectMapper;

  @PostMapping("validator-default")
  public Params defaultGroup(@RequestBody @Validated final Params params) throws JsonProcessingException {
    log.info("params: {}", objectMapper.writeValueAsString(params));
    return params;
  }

  @PostMapping("validator-save")
  public Params saveGroup(@RequestBody @Validated(SaveOne.class) final Params params) throws JsonProcessingException {
    log.info("params: {}", objectMapper.writeValueAsString(params));
    return params;
  }

  @PostMapping("validator-update")
  public Params updateGroup(@RequestBody @Validated(UpdateOne.class) final Params params) throws JsonProcessingException {
    log.info("params: {}", objectMapper.writeValueAsString(params));
    return params;
  }

  @Data
  static class Params {

    @NotNull(message = "日期时间不能为 null", groups = SaveOne.class)
    private LocalDateTime dateTime;

    @NotNull(message = "类型不能为 null", groups = UpdateOne.class)
    private Type type;

    @NotNull(message = "布尔值不能为 null")
    private Boolean bool;

    @Valid
    @NotNull(message = "用户信息不能为 null", groups = SaveOne.class)
    private User user;
  }

  @Getter
  @ToString
  @RequiredArgsConstructor
  enum Type {

    BOOK(1),
    INTERNET(2);

    @JsonValue
    private final int code;
  }

  @Data
  static class User {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
  }
}
