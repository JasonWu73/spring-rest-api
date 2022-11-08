package net.wuxianjie.springrestapi.hikvision;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HkTestController {

  private final HkSdkComponent hkSdk;

  @GetMapping("/api/v1/public/hik/test-set")
  public void test() {
    hkSdk.setOsd(
      "192.168.133.22",
      8000,
      "admin",
      "admin12345",
      1,
      new String[]{
        "案号：（2022）浙0382刑初790案件",
        "庭次：1",
        "案由：离婚纠纷",
        "法庭：附楼法庭1",
        "主审法官：杨志敏",
        "书记员：陈佳佳",
        "原告：沈洪英",
        "被告：朱小明"
      }
    );
  }

  @GetMapping("/api/v1/public/hik/test-clear")
  public void testClear() {
    hkSdk.clearOsd(
      "192.168.133.22",
      8000,
      "admin",
      "admin12345",
      1
    );
  }
}
