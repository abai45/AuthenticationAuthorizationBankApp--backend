package kz.group.reactAndSpring.resource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kz.group.reactAndSpring.domain.Response;
import kz.group.reactAndSpring.dto.*;
import kz.group.reactAndSpring.service.BankCardService;
import kz.group.reactAndSpring.service.TokenService;
import kz.group.reactAndSpring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static kz.group.reactAndSpring.constant.Constants.IMAGE_DIRECTORY;
import static kz.group.reactAndSpring.utils.RequestUtils.getResponse;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/user"})
public class UserResource {
    private final UserService userService;
    private final TokenService tokenService;
    private final BankCardService bankCardService;

    @PostMapping("/register")
    public ResponseEntity<UserTokenResponseDto> saveUser(@RequestBody @Valid UserRequestDto user, HttpServletRequest request) {
        var clientIp = request.getRemoteAddr();
        userService.createUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword(),user.getPhone(), clientIp);
        UserDto newUser = userService.getUserByEmail(user.getEmail());
        UserTokenResponseDto tokenResponse = tokenService.generateTokens(newUser);
        return ResponseEntity.created(getUri()).body(tokenResponse);
    }

    @PostMapping("/login/otp")
    public ResponseEntity<UserTokenResponseDto> verifyOtp(@RequestBody @Valid OtpCodeRequestDto otpRequest, HttpServletRequest request) {
        var user = userService.userOtpVerify(otpRequest.getEmail(), otpRequest.getOtpCode());
        UserTokenResponseDto tokenResponse = tokenService.generateTokens(user);
        return ResponseEntity.ok().body(tokenResponse);
    }

    @PatchMapping("/mfa/set")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('USER','ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> setMfa(@AuthenticationPrincipal UserDto userPrincipal, HttpServletRequest request) {
        var user = userService.setMfa(userPrincipal.getId());
        return ResponseEntity.ok().body(getResponse(request,Map.of("user",user), "MFA is changed successfully", OK));
    }

    @GetMapping("/mfa/status")
    public ResponseEntity<Response> statusMfa(@AuthenticationPrincipal UserDto userPrincipal, HttpServletRequest request) {
        var user = userService.getUserByEmail(userPrincipal.getEmail());
        return ResponseEntity.ok().body(getResponse(request,Map.of("user",user), "MFA status is " + user.isMfa(), OK));
    }

    @PostMapping("/resendotp")
    public ResponseEntity<Response> resendOtp(@RequestBody @Valid EmailRequestDto emailRequestDto, HttpServletRequest request) {
        userService.sendOtpCodeMessage(emailRequestDto.getEmail());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "We send new OTP code to email", OK));
    }

    @PostMapping("/verify/account/otp")
    public ResponseEntity<Response> verifyOtpAccount(@RequestBody @Valid OtpCodeRequestDto otpCodeRequest, HttpServletRequest request) {
        userService.verifyOtpCode(otpCodeRequest.getEmail(), otpCodeRequest.getOtpCode());
        return ResponseEntity.ok().body(getResponse(request,emptyMap(), "Otp code is valid", OK));
    }

    @PatchMapping("/updatepassword")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('USER','ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> updatePassword(@AuthenticationPrincipal UserDto user, @RequestBody UpdatePasswordRequest passwordRequest, HttpServletRequest request) {
        userService.updatePassword(user.getUserId(), passwordRequest.getPassword(), passwordRequest.getNewPassword(), passwordRequest.getConfirmNewPassword());
        return ResponseEntity.ok().body(getResponse(request,emptyMap(), "Password updated successfully", OK));
    }

    //forgot pass
    @PostMapping("/resetpassword")
    public ResponseEntity<Response> resetPassword(@RequestBody @Valid EmailRequestDto emailRequest, HttpServletRequest request) {
        userService.resetPassword(emailRequest.getEmail());
        return ResponseEntity.ok().body(getResponse(request,emptyMap(), "We sent you an email to reset your password", OK));
    }

    @GetMapping("/verify/password")
    public ResponseEntity<Response> verifyPassword(@RequestParam("key") String key, HttpServletRequest request) {
        var user = userService.verifyConfirmationKey(key);
        return ResponseEntity.ok().body(getResponse(request,Map.of("user",user), "Enter new password", OK));
    }

    @PostMapping("/resetpassword/reset")
    public ResponseEntity<Response> doResetPassword(@RequestBody @Valid ResetPasswordRequestDto resetPasswordRequest, HttpServletRequest request) {
        userService.updatePassword(resetPasswordRequest.getUserId(), resetPasswordRequest.getNewPassword(), resetPasswordRequest.getConfirmNewPassword());
        return ResponseEntity.ok().body(getResponse(request,emptyMap(), "Password reset successfully", OK));
    }

    @PostMapping("/getbalance")
    public ResponseEntity<Response> getUserTotalBalance(@AuthenticationPrincipal UserDto user, HttpServletRequest request) {
        var totalBalance = bankCardService.getTotalBalance(user);
        return ResponseEntity.ok(getResponse(request, Map.of("totalBalance", totalBalance), "Total balance retrieved successfully", OK));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('user:read') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> getUsers(@AuthenticationPrincipal UserDto user, HttpServletRequest request) {
        return ResponseEntity.ok().body(getResponse(request,Map.of("users",userService.getUsers()), "Users list retrieved", OK));
    }

    //Profile
    @GetMapping("/profile")
    @PreAuthorize("hasAnyAuthority('user:read') or hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> profile(@AuthenticationPrincipal UserDto userPrincipal, HttpServletRequest request) {
        var user = userService.getUserByUserId(userPrincipal.getUserId());
        return ResponseEntity.ok().body(getResponse(request,Map.of("user",user), "Profile retrieved", OK));
    }

    @PatchMapping("/update")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('USER','ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> update(@AuthenticationPrincipal UserDto userPrincipal, @RequestBody UserRequestDto userRequest, HttpServletRequest request) {
        var user = userService.updateUser(userPrincipal.getUserId(), userRequest.getFirstName(), userRequest.getLastName(), userRequest.getEmail(), userRequest.getPhone());
        return ResponseEntity.ok().body(getResponse(request,Map.of("user",user), "User updated successfully", OK));
    }

    @PatchMapping("/updaterole")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> updateRole(@AuthenticationPrincipal UserDto userPrincipal, @RequestBody RoleRequestDto roleRequest, HttpServletRequest request) {
        userService.updateRole(userPrincipal.getUserId(), roleRequest.getRole());
        return ResponseEntity.ok().body(getResponse(request,emptyMap(), "Role updated successfully", OK));
    }

    @PatchMapping("/photo")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('USER','ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> uploadPhoto(@AuthenticationPrincipal UserDto user, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        var imageUrl = userService.uploadPhoto(user.getUserId(), file);
        return ResponseEntity.ok().body(getResponse(request,Map.of("imageUrl", imageUrl), "Photo updated successfully", OK));
    }

    @GetMapping(value = "/images/{filename}", produces = { IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE})
    public byte[] getPhoto(@PathVariable("filename") String filename) throws IOException {
        return Files.readAllBytes(Paths.get(IMAGE_DIRECTORY + filename));
    }

    @PatchMapping("/toggleaccountexpired")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> toggleAccountExpired(@AuthenticationPrincipal UserDto user, HttpServletRequest request) {
        userService.toggleAccountExpired(user.getUserId());
        return ResponseEntity.ok().body(getResponse(request,emptyMap(), "Account updated successfully", OK));
    }

    @PatchMapping("/toggleaccountlocked")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> toggleAccountLocked(@AuthenticationPrincipal UserDto user, HttpServletRequest request) {
        userService.toggleAccountLocked(user.getUserId());
        return ResponseEntity.ok().body(getResponse(request,emptyMap(), "Account updated successfully", OK));
    }

    @PatchMapping("/toggleaccountenabled")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> toggleAccountEnabled(@AuthenticationPrincipal UserDto user, HttpServletRequest request) {
        userService.toggleAccountEnabled(user.getUserId());
        return ResponseEntity.ok().body(getResponse(request,emptyMap(), "Account updated successfully", OK));
    }

    @PostMapping("/delete")
    public ResponseEntity<Response> deleteUser(@RequestBody @Valid EmailRequestDto emailRequest, HttpServletRequest request) {
        userService.deleteUser(emailRequest.getEmail());
        return ResponseEntity.ok().body(getResponse(request,emptyMap(), "User was deleted successfully", OK));
    }

    private URI getUri() {
        return URI.create("");
    }
}
