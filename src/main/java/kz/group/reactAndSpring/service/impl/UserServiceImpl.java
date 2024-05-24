package kz.group.reactAndSpring.service.impl;

import kz.group.reactAndSpring.event.listener.UserEventListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import kz.group.reactAndSpring.domain.RequestContext;
import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.entity.ConfirmationEntity;
import kz.group.reactAndSpring.entity.CredentialEntity;
import kz.group.reactAndSpring.entity.RoleEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.enumeration.AuthorityEnum;
import kz.group.reactAndSpring.enumeration.EventType;
import kz.group.reactAndSpring.enumeration.LoginType;
import kz.group.reactAndSpring.event.UserEvent;
import kz.group.reactAndSpring.exception.ApiException;
import kz.group.reactAndSpring.repository.ConfirmationRepository;
import kz.group.reactAndSpring.repository.CredentialRepository;
import kz.group.reactAndSpring.repository.RoleRepository;
import kz.group.reactAndSpring.repository.UserRepository;
import kz.group.reactAndSpring.service.EmailService;
import kz.group.reactAndSpring.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static kz.group.reactAndSpring.constant.Constants.IMAGE_DIRECTORY;
import static kz.group.reactAndSpring.enumeration.EventType.RESETPASSWORD;
import static kz.group.reactAndSpring.utils.UserUtils.*;
import static kz.group.reactAndSpring.validation.UserValidation.verifyAccountStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CredentialRepository credentialRepository;
    private final ConfirmationRepository confirmationRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder encoder;
    private final ApplicationEventPublisher publisher;

    @Override
    public void createUser(String firstName, String lastName, String email, String password, String phone, String clientIp) {
        var userEntity = userRepository.save(createNewUser(firstName, lastName, email, phone, clientIp));
        var credentialEntity = new CredentialEntity(userEntity, encoder.encode(password));
        credentialRepository.save(credentialEntity);
        var confirmationEntity = new ConfirmationEntity(userEntity);
        confirmationRepository.save(confirmationEntity);
        publisher.publishEvent(new UserEvent(userEntity, EventType.REGISTRATION, Map.of("key", confirmationEntity.getKey())));
    }

    private UserEntity createNewUser(String firstName, String lastName, String email, String phone, String clientIp) {
        var role = getRoleName(AuthorityEnum.USER.name());
        return createUserEntity(firstName,lastName,email, phone, role, clientIp);
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
                if(userEntity.getLockTime() != null && userEntity.getLockTime().plusMinutes(15).isAfter(now())) {
                    throw new ApiException("Your account is locked for 15 minutes");
                } else {
                    if(userEntity.getLockTime() != null && userEntity.getLockTime().plusMinutes(15).isBefore(now())) {
                        userEntity.setAccountNonLocked(true);
                        userEntity.setLoginAttempts(0);
                        userEntity.setLockTime(null);
                    }
                    userEntity.setLoginAttempts(userEntity.getLoginAttempts() + 1);
                    if(userEntity.getLoginAttempts() >= 5) {
                        userEntity.setAccountNonLocked(false);
                        userEntity.setLockTime(now());
                    }
                }
            }
            case LOGIN_SUCCESS -> {
                userEntity.setAccountNonLocked(true);
                userEntity.setLoginAttempts(0);
                userEntity.setLastLogin(now());
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
    public void updatePassword(String userId, String currentPassword, String newPassword, String confirmNewPassword) {
        if(!confirmNewPassword.equals(newPassword)) {
            throw new ApiException("Password does not match. Please try again.");
        }
        var user = getUserEntityByUserId(userId);
        verifyAccountStatus(user);
        var credentials = getUserCredentialById(user.getId());
        if(!encoder.matches(currentPassword, credentials.getPassword())) {
            throw new ApiException("Existing password does not match. Please try again.");
        }
        credentials.setPassword(encoder.encode(newPassword));
        credentialRepository.save(credentials);
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
       return fromUserEntity(userEntity, userEntity.getRoles(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public void updatePassword(String userId, String newPassword, String confirmNewPassword) {
        if(!confirmNewPassword.equals(newPassword)) {
            throw new ApiException("Password does not match. Please try again.");
        }
        var user = getUserByUserId(userId);
        var userEntity = getUserEntityByUserId(userId);
        var confirmationEntity = getUserConfirmation(userEntity);
        var credentials = getUserCredentialById(user.getId());
        credentials.setPassword(encoder.encode(newPassword));
        if(!userEntity.isEnabled()) {
            userEntity.setEnabled(true);
            userRepository.save(userEntity);
        }
        confirmationRepository.delete(confirmationEntity);
        credentialRepository.save(credentials);
    }

    @Override
    public void verifyOtpCode(String email,String otpCode) {
        var user = getUserEntityByEmail(email);
        if(!otpCode.equals(user.getOtpCode())) {
            throw new ApiException("Incorrect OTP code");
        }
        if(user.getTokenCreatedAt() != null && user.getTokenCreatedAt().plusMinutes(10).isBefore(now())) {
            sendOtpCodeMessage(user.getEmail());
            throw new ApiException("OTP code validation timed out. Please check your email and try again.");
        }
        var confirmationEntity = getUserConfirmation(user);
        user.setEnabled(true);
        user.setOtpCode(null);
        user.setTokenCreatedAt(null);
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
    public UserDto userOtpVerify(String email, String otpCode) {
        var userEntity = getUserEntityByEmail(email);
        if (!otpCode.equals(userEntity.getOtpCode())) {
            throw new ApiException("OTP code is not correct");
        }
        if(userEntity.getTokenCreatedAt()!=null && userEntity.getTokenCreatedAt().plusMinutes(5).isBefore(now())) {
            sendOtpCodeMessage(email);
            throw new ApiException("OTP code validation timed out. Please try again.");
        }
        userEntity.setOtpCode(null);
        userEntity.setTokenCreatedAt(null);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRoles(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendOtpCodeMessage(String email) {
        var user = getUserEntityByEmail(email);
        var otpCode = generateOtpCode();
        user.setOtpCode(otpCode);
        user.setTokenCreatedAt(now());
        userRepository.save(user);
        emailService.sendOtpMessageHtmlPage(user.getFirstName(), email, otpCode);
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
    public List<UserDto> getUsers() {
        return userRepository.findAll()
                .stream()
                .filter(userEntity -> !"system@gmail.com".equalsIgnoreCase(userEntity.getEmail()))
                .map(userEntity -> fromUserEntity(userEntity, userEntity.getRoles(), getUserCredentialById(userEntity.getId())))
                .collect(toList());
    }

    @Override
    public void sendLocationValidateLink(String email) {
        var userEntity = getUserEntityByEmail(email);
        var confirmationEntity = new ConfirmationEntity(userEntity);
        confirmationRepository.save(confirmationEntity);
        publisher.publishEvent(new UserEvent(userEntity, EventType.IPADDRESSVERIFY, Map.of("ok", confirmationEntity.getKey())));
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


    @Override
    public void toggleAccountExpired(String userId) {
        var user = getUserEntityByUserId(userId);
        user.setAccountNonExpired(!user.isAccountNonExpired());
        userRepository.save(user);
    }

    @Override
    public void toggleAccountLocked(String userId) {
        var user = getUserEntityByUserId(userId);
        user.setAccountNonLocked(!user.isAccountNonLocked());
        userRepository.save(user);
    }

    @Override
    public void toggleAccountEnabled(String userId) {
        var user = getUserEntityByUserId(userId);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Override
    public String uploadPhoto(String userId, MultipartFile file) {
        var user = getUserEntityByUserId(userId);
        var photoUrl = photoFunction.apply(userId, file);
        user.setImgUrl(photoUrl + "?timestamp=" + System.currentTimeMillis());
        userRepository.save(user);
        return photoUrl;
    }

    private final BiFunction<String, MultipartFile, String> photoFunction = (id, file) -> {
        var filename = id + ".png";
        try {
            var fileStorageLocation = Paths.get(IMAGE_DIRECTORY).toAbsolutePath().normalize();
            if(!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
            Files.copy(file.getInputStream(), fileStorageLocation.resolve(filename), REPLACE_EXISTING);
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/user/images/" + filename).toUriString();
        } catch (Exception e) {
            throw new ApiException("Unable to save image");
        }
    };

    private UserEntity getUserEntityByUserId(String userId) {
        var userByUserId = userRepository.findUserByUserId(userId);
        return userByUserId.orElseThrow(() -> new ApiException("User by user id not found"));
    }

    private UserEntity getUserEntityById(Long id) {
        var userById = userRepository.findById(id);
        return userById.orElseThrow(() -> new ApiException("User by id not found"));
    }

    private ConfirmationEntity getUserConfirmation(String key) {
        return confirmationRepository.findByKey(key).orElseThrow(() -> new ApiException("Confirmation is not found"));
    }
    private ConfirmationEntity getUserConfirmation(UserEntity user) {
        return confirmationRepository.findByUserEntity(user).orElse(null);
    }
    private UserEntity getUserEntityByEmail(String email) {
        var userByEmail = userRepository.findByEmailIgnoreCase(email);
        return userByEmail.orElseThrow(() -> new ApiException("User by email is not found"));
    }
}
