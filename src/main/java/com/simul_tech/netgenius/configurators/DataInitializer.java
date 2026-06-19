package com.simul_tech.netgenius.configurators;

import com.simul_tech.netgenius.enums.Role;
import com.simul_tech.netgenius.models.Lab;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.repositories.LabRepository;
import com.simul_tech.netgenius.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Идемпотентная инициализация справочных данных на старте приложения:
 * создаёт администратора по умолчанию и набор демонстрационных лабораторных,
 * если они ещё отсутствуют. Выполняется после создания схемы Hibernate.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final LabRepository labRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@netgenius.ru}")
    private String adminEmail;

    @Value("${app.admin.password:Admin12345}")
    private String adminPassword;

    @Value("${app.seed.labs:true}")
    private boolean seedLabs;

    @Override
    public void run(String... args) {
        seedAdmin();
        if (seedLabs) {
            seedLabs();
        }
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        User admin = new User();
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setNickname("admin");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);
        log.info("Создан администратор по умолчанию: {}", adminEmail);
    }

    private void seedLabs() {
        if (labRepository.count() > 0) {
            return;
        }
        List<String[]> demo = List.of(
                new String[]{"Основы коммутации", "Настройка коммутатора и VLAN"},
                new String[]{"Маршрутизация", "Статическая и динамическая маршрутизация"},
                new String[]{"Подсети и IP-адресация", "Разбиение сети на подсети, маски"},
                new String[]{"Протоколы канального уровня", "ARP, STP, агрегация каналов"},
                new String[]{"Безопасность сети", "ACL, port-security, базовый firewall"}
        );
        int order = 1;
        for (String[] item : demo) {
            Lab lab = new Lab();
            lab.setTitle(item[0]);
            lab.setDescription(item[1]);
            lab.setSortOrder(order++);
            lab.setActive(true);
            labRepository.save(lab);
        }
        log.info("Создано {} демонстрационных лабораторных работ", demo.size());
    }
}
