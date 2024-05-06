package kz.group.reactAndSpring.service;

import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.entity.CredentialEntity;
import kz.group.reactAndSpring.entity.RoleEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.enumeration.AuthorityEnum;
import kz.group.reactAndSpring.repository.CredentialRepository;
import kz.group.reactAndSpring.repository.UserRepository;
import kz.group.reactAndSpring.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private CredentialRepository credentialRepository;
    @InjectMocks
    private UserServiceImpl userServiceImpl;


    @Test
    @DisplayName("Test Find user by id")
    public void testFindById(){
        // Arrange - Given
        var userEntity = new UserEntity();
        userEntity.setFirstName("Abai");
        userEntity.setId(1L);
        userEntity.setUserId("1");
        userEntity.setCreatedAt(LocalDateTime.of(1990,11,1,11,11,11));
        userEntity.setUpdatedAt(LocalDateTime.of(1990,11,1,11,11,11));
        userEntity.setLastLogin(LocalDateTime.of(1990,11,1,11,11,11));


        var roleEntity = new RoleEntity("USER", AuthorityEnum.USER);
        userEntity.setRoles(roleEntity);

        var credentialEntity = new CredentialEntity();
        credentialEntity.setUpdatedAt(LocalDateTime.of(1990,11,1,11,11,11));
        credentialEntity.setUserEntity(userEntity);
        credentialEntity.setPassword("password");

        // Act - When
        when(userRepository.findUserByUserId("1")).thenReturn(Optional.of(userEntity));
        when(credentialRepository.getCredentialByUserEntityId(1L)).thenReturn(Optional.of(credentialEntity));
        var userByUserId = userServiceImpl.getUserByUserId("1");
        // Assert - Then
        assertThat(userByUserId.getFirstName()).isEqualTo(userEntity.getFirstName());
        assertThat(userByUserId.getUserId()).isEqualTo("1");
    }
}
