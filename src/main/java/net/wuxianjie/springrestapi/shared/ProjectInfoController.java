package net.wuxianjie.springrestapi.shared;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

@RestController
public class ProjectInfoController {

  @GetMapping("/api/v1/version")
  public ResponseEntity<LinkedHashMap<String, Object>> getVersion() {
    return ResponseEntity.ok(new LinkedHashMap<>() {{
      put("version", "v1.0.0");
      put("built", LocalDateTime.now());
    }});
  }

}
