package cn.sp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.project.rpc.annotation.RpcServiceProvider;

@RpcServiceProvider
public class UserServiceImpl implements UserService{

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    @Override
    public ApiResult<User> getUser(Long id) {
        logger.info("现在是【3】号提供服务");
        User user = new User(1L,"XX",2,"www.aa.com");
        return ApiResult.success(user);
    }
}
