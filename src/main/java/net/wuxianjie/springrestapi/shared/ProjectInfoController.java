package net.wuxianjie.springrestapi.shared;

import net.wuxianjie.springrestapi.shared.util.StrUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/v1")
public class ProjectInfoController {

  /**
   * 获取版本信息.
   *
   * @return <pre>{@code
   * {
   *   "version": "v1.0.0", // 版本号
   *   "developer": "吴仙杰", // 开发者
   *   "machineCode": "..." // 机器码
   * }
   * }</pre>
   */
  @GetMapping("public/version")
  public ResponseEntity<LinkedHashMap<String, Object>> getVersion() {
    return ResponseEntity.ok(new LinkedHashMap<>() {{
      put("version", "v1.0.0");
      put("developer", "吴仙杰");
      put("machineCode", StrUtils.getMachineCode().orElse("unknown"));
    }});
  }
}
