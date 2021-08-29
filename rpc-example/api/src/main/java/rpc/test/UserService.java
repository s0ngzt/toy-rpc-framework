package rpc.test;

public interface UserService {
    ApiResult<User> getUser(Long id);
}
