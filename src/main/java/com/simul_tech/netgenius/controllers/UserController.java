import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import com.simul_tech.netgenius.entities.User;
import com.simul_tech.netgenius.models.UserDTO;
import com.simul_tech.netgenius.models.UserMapper;
import com.simul_tech.netgenius.repositories.UserRepository;
import java.util.Optional;

@Tag(name = "Пользователи", description = "Операции с пользователями")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Operation(summary = "Создать пользователя", description = "Создает нового пользователя в системе")
    @PostMapping
    public UserDTO createUser(@RequestBody UserDTO userDto) {
        User entity = UserMapper.toEntity(userDto);
        userRepository.save(entity);
        return UserMapper.toDto(entity);
    }

    @Operation(summary = "Получить пользователя по ID", description = "Возвращает пользователя по его уникальному идентификатору")
    @GetMapping("/{id}")
    public UserDTO getUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        return userOpt.map(UserMapper::toDto).orElse(null);
    }

    // и так далее для других методов
}