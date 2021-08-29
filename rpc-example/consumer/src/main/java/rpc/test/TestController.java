package rpc.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.project.rpc.annotation.RpcServiceConsumer;

@RestController
@RequestMapping("test")
public class TestController {

    @RpcServiceConsumer
    private UserService userService;

    @GetMapping("/user")
    public ApiResult<User> getUser(@RequestParam("id") Long id) {
        return userService.getUser(id);
    }
}
