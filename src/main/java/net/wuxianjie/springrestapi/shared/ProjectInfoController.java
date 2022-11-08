package net.wuxianjie.springrestapi.shared;

import net.wuxianjie.springrestapi.shared.util.StrUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;

@RestController
public class ProjectInfoController {

  /**
   * 获取版本信息.
   *
   * @return <pre>{@code
   * {
   *   "version": "v1.0.0", // 版本号
   *   "developer": "吴仙杰", // 开发者
   *   "machineCode": "63342D34312D31652D62322D31322D3034" // 机器码
   * }
   * }</pre>
   */
  @GetMapping("/api/v1/version")
  public ResponseEntity<LinkedHashMap<String, Object>> getVersion() {
    return ResponseEntity.ok(new LinkedHashMap<>() {{
      put("version", "v1.0.0");
      put("developer", "吴仙杰");
      put("machineCode", StrUtils.getMachineCode());
    }});
  }
}
