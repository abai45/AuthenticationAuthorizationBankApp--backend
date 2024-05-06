package kz.group.reactAndSpring.service.impl;

import jakarta.transaction.Transactional;
import kz.group.reactAndSpring.cache.CacheStore;
import kz.group.reactAndSpring.domain.RequestContext;
import kz.group.reactAndSpring.domain.UserPrincipal;
import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.dto.UserTokenResponseDto;
import kz.group.reactAndSpring.entity.ConfirmationEntity;
import kz.group.reactAndSpring.entity.CredentialEntity;
import kz.group.reactAndSpring.entity.RoleEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.enumeration.AuthorityEnum;
import kz.group.reactAndSpring.enumeration.EventType;
import kz.group.reactAndSpring.enumeration.LoginType;
import kz.group.reactAndSpring.event.UserEvent;
import kz.group.reactAndSpring.domain.Token;
import kz.group.reactAndSpring.exception.ApiException;
import kz.group.reactAndSpring.repository.ConfirmationRepository;
import kz.group.reactAndSpring.repository.CredentialRepository;
import kz.group.reactAndSpring.repository.RoleRepository;
import kz.group.reactAndSpring.repository.UserRepository;
import kz.group.reactAndSpring.service.JwtService;
import kz.group.reactAndSpring.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static kz.group.reactAndSpring.enumeration.EventType.RESETPASSWORD;
import static kz.group.reactAndSpring.utils.UserUtils.*;
import static kz.group.reactAndSpring.validation.UserValidation.verifyAccountStatus;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CredentialRepository credentialRepository;
    private final ConfirmationRepository confirmationRepository;
    private final CacheStore<String, Integer> userCacheStore;
    private final BCryptPasswordEncoder encoder;
    private final ApplicationEventPublisher publisher;
    @Override
    public void createUser(String firstName, String lastName, String email, String password) {
        var userEntity = userRepository.save(createNewUser(firstName, lastName, email));
        var credentialEntity = new CredentialEntity(userEntity, encoder.encode(password));
        credentialRepository.save(credentialEntity);
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
    public UserDto verifyAccountKey(String key) {
        var confirmationEntity = getUserConfirmation(key);
        var userEntity = getUserEntityByEmail(confirmationEntity.getUserEntity().getEmail());
        if (confirmationEntity == null) {
            throw new ApiException("Unable to find token");
        }
        if (userEntity == null) {
            throw new ApiException("Incorrect token");
        }
        return fromUserEntity(userEntity, userEntity.getRoles(), getUserCredentialById(userEntity.getId()));
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
        var userEntity = userRepository.findUserByUserId(userId).orElseThrow(() -> new ApiException("User not found"));
        return fromUserEntity(userEntity, userEntity.getRoles(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public UserDto getUserByEmail(String email) {
        UserEntity userEntity = getUserEntityByEmail(email);
        return fromUserEntity(userEntity, userEntity.getRoles(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public CredentialEntity getUserCredentialById(Long userId) {
        var credentialById = credentialRepository.getCredentialByUserEntityId(userId);
        return credentialById.orElseThrow(() -> new ApiException("Unable to find user credential"));
    }

    @Override
    public void resetPassword(String email) {
        var user = getUserEntityByEmail(email);
        var confirmation = getUserConfirmation(user);
        if (confirmation != null) {
            publisher.publishEvent(new UserEvent(user, RESETPASSWORD, Map.of("key", confirmation.getKey())));
        } else {
            var confirmationEntity = new ConfirmationEntity(user);
            confirmationRepository.save(confirmationEntity);
            publisher.publishEvent(new UserEvent(user, RESETPASSWORD, Map.of("key", confirmationEntity.getKey())));
        }
    }

    @Override
    public UserDto verifyConfirmationKey(String key) {
       var confirmationEntity = getUserConfirmation(key);
       if (confirmationEntity == null) {
           throw new ApiException("Unable to find token");
       }
       var userEntity = getUserEntityByEmail(confirmationEntity.getUserEntity().getEmail());
       if (userEntity == null) {
           throw new ApiException("Incorrect token");
       }
       verifyAccountStatus(userEntity);
       confirmationRepository.delete(confirmationEntity);
       return fromUserEntity(userEntity, userEntity.getRoles(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public void updatePassword(String userId, String newPassword, String confirmNewPassword) {
        if(!confirmNewPassword.equals(newPassword)) {
            throw new ApiException("Password does not match. Please try again.");
        }
        var user = getUserByUserId(userId);
        var credentials = getUserCredentialById(user.getId());
        credentials.setPassword(encoder.encode(newPassword));
        credentialRepository.save(credentials);
    }

    @Override
    public void verifyOtpCode(String otpCode) {
        var user = getUserEntityByOtpCode(otpCode);
        if(!user.getOtpCode().equals(otpCode)) {
            throw new ApiException("Incorrect otp code");
        }
        var confirmationEntity = getUserConfirmation(user);
        user.setEnabled(true);
        user.setOtpCode(null);
        userRepository.save(user);
        confirmationRepository.delete(confirmationEntity);
    }

    @Override
    public UserDto setMfa(Long id) {
        var userEntity = getUserEntityById(id);
        if (userEntity.isMfa()) {
            userEntity.setMfa(false);
        } else {
            userEntity.setMfa(true);
        }
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRoles(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public void deleteUser(String email) {
        var user = getUserEntityByEmail(email);
        if (user == null) {
            throw new ApiException("User not found");
        }
        userRepository.delete(user);
    }

    @Override
    public void saveOtpCode(String email, String otpCode) {
        var user = getUserEntityByEmail(email);
        user.setOtpCode(otpCode);
        userRepository.save(user);
    }

    @Override
    public UserDto UserOtpVerify(String otpCode) {
        var userEntity = getUserEntityByOtpCode(otpCode);
        if (userEntity == null) {
            throw new ApiException("User not found");
        }
        userEntity.setOtpCode(null);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRoles(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public UserDto updateUser(String userId, String firstName, String lastName, String email, String phone) {
        var userEntity = getUserEntityByUserId(userId);
        userEntity.setFirstName(firstName);
        userEntity.setLastName(lastName);
        userEntity.setEmail(email);
        userEntity.setPhone(phone);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRoles(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public void updateRole(String userId, String role) {
        var userEntity = getUserEntityByUserId(userId);
        userEntity.setRoles(getRoleName(role));
        userRepository.save(userEntity);
    }

    private UserEntity getUserEntityByUserId(String userId) {
        var userByUserId = userRepository.findUserByUserId(userId);
        return userByUserId.orElseThrow(() -> new ApiException("User not found"));
    }

    private UserEntity getUserEntityById(Long id) {
        var userById = userRepository.findById(id);
        return userById.orElseThrow(() -> new ApiException("User not found"));
    }

    private ConfirmationEntity getUserConfirmation(String key) {
        return confirmationRepository.findByKey(key).orElseThrow(() -> new ApiException("Confirmation is not found"));
    }
    private ConfirmationEntity getUserConfirmation(UserEntity user) {
        return confirmationRepository.findByUserEntity(user).orElse(null);
    }
    private UserEntity getUserEntityByEmail(String email) {
        var userByEmail = userRepository.findByEmailIgnoreCase(email);
        return userByEmail.orElseThrow(() -> new ApiException("User is not found"));
    }
    private UserEntity getUserEntityByOtpCode(String otp) {
        var userByEmail = userRepository.findUserByOtpCode(otp);
        return userByEmail.orElseThrow(() -> new ApiException("User is not found"));
    }
}
