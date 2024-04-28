package kz.group.reactAndSpring.service.impl;

import jakarta.transaction.Transactional;
import kz.group.reactAndSpring.cache.CacheStore;
import kz.group.reactAndSpring.domain.RequestContext;
import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.entity.ConfirmationEntity;
import kz.group.reactAndSpring.entity.CredentionalEntity;
import kz.group.reactAndSpring.entity.RoleEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.enumeration.AuthorityEnum;
import kz.group.reactAndSpring.enumeration.EventType;
import kz.group.reactAndSpring.enumeration.LoginType;
import kz.group.reactAndSpring.event.UserEvent;
import kz.group.reactAndSpring.exception.ApiException;
import kz.group.reactAndSpring.repository.ConfirmationRepository;
import kz.group.reactAndSpring.repository.CredentionalRepository;
import kz.group.reactAndSpring.repository.RoleRepository;
import kz.group.reactAndSpring.repository.UserRepository;
import kz.group.reactAndSpring.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.time.LocalDateTime.now;
import static kz.group.reactAndSpring.utils.UserUtils.createUserEntity;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CredentionalRepository credentionalRepository;
    private final ConfirmationRepository confirmationRepository;
    private final CacheStore<String, Integer> userCacheStore;
//    private final BCryptPasswordEncoder encoder;
    private final ApplicationEventPublisher publisher;

    @Override
    public void createUser(String firstName, String lastName, String email, String password) {
        var userEntity = userRepository.save(createNewUser(firstName, lastName, email));
        var credentionalEntity = new CredentionalEntity(userEntity, password);
        credentionalRepository.save(credentionalEntity);
        var confirmationEntity = new ConfirmationEntity(userEntity);
        confirmationRepository.save(confirmationEntity);
        publisher.publishEvent(new UserEvent(userEntity, EventType.REGISTRATION, Map.of("key", confirmationEntity.getKey())));
    }
    private UserEntity createNewUser(String firstName, String lastName, String email) {
        var role = getRoleName(AuthorityEnum.USER.name());
        return createUserEntity(firstName,lastName,email,role);
    }
    @Override
    public RoleEntity getRoleName(String name) {
        var role = roleRepository.findByNameIgnoreCase(name);
        return role.orElseThrow(() -> new ApiException("Role is not found"));
    }

    @Override
    public void verifyAccountKey(String key) {
        var confirmationEntity = getUserConfirmation(key);
        var userEntity = getUserEntityByEmail(confirmationEntity.getUserEntity().getEmail());
        userEntity.setEnabled(true);
        userRepository.save(userEntity);
        confirmationRepository.delete(confirmationEntity);
    }

    @Override
    public void updateLoginAttempt(String email, LoginType loginType) {
        var userEntity = getUserEntityByEmail(email);
        RequestContext.setUserId(userEntity.getId());
        switch (loginType) {
            case LOGIN_ATTEMPT -> {
                if(userCacheStore.get(userEntity.getEmail()) == null) {
                    userEntity.setLoginAttempts(0);
                    userEntity.setAccountNonLocked(true);
                }
                userEntity.setLoginAttempts(userEntity.getLoginAttempts() + 1);
                userCacheStore.put(userEntity.getEmail(), userEntity.getLoginAttempts());
                if (userCacheStore.get(userEntity.getEmail()) > 5) {
                    userEntity.setAccountNonLocked(false);
                }
            }
            case LOGIN_SUCCESS -> {
                userEntity.setAccountNonLocked(true);
                userEntity.setLoginAttempts(0);
                userEntity.setLastLogin(now());
                userCacheStore.remove(userEntity.getEmail());
            }
        }
        userRepository.save(userEntity);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        var userEntity = getUserEntityByUserId(userId);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    private CredentionalEntity getUserCredentialById(Long id) {
        var credentionalEntity = credentionalRepository.findById(id);
        return credentionalEntity.orElseThrow(() -> new ApiException("Credentional is not found"));
    }

    private UserEntity getUserEntityByEmail(String email) {
        var userByEmail = userRepository.findByEmailIgnoreCase(email);
        return userByEmail.orElseThrow(() -> new ApiException("User is not found"));
    }
    private UserEntity getUserEntityByUserId(String userId) {
        var userByUserId = userRepository.findByUserId(userId);
        return userByUserId.orElseThrow(() -> new ApiException("User is not found"));
    }

    private ConfirmationEntity getUserConfirmation(String key) {
        return confirmationRepository.findByKey(key).orElseThrow(() -> new ApiException("Confirmation is not found"));
    }
}
