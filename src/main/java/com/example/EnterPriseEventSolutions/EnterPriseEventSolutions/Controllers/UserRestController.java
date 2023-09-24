package com.example.EnterPriseEventSolutions.EnterPriseEventSolutions.Controllers;


import com.example.EnterPriseEventSolutions.EnterPriseEventSolutions.Models.ConfirmationToken;
import com.example.EnterPriseEventSolutions.EnterPriseEventSolutions.Models.User;
import com.example.EnterPriseEventSolutions.EnterPriseEventSolutions.Models.UserTipeEnum;
import com.example.EnterPriseEventSolutions.EnterPriseEventSolutions.Services.EmailService.ConfirmationTokenService;
import com.example.EnterPriseEventSolutions.EnterPriseEventSolutions.Services.EmailService.EmailService;
import com.example.EnterPriseEventSolutions.EnterPriseEventSolutions.Services.EmailService.RegisterService;
import com.example.EnterPriseEventSolutions.EnterPriseEventSolutions.Services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping("/api")
public class UserRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ConfirmationTokenService confirmationTokenService;

    @Autowired
    private RegisterService registerService;


    //POST members
    @Operation(summary = "Post a new member")

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Created",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation= User.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content
            )
    })
    @JsonView(User.BasicInfo.class)
    @PostMapping("/users/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> createMember(@RequestBody User user) {
        try {
            LocalDateTime currentDate = LocalDateTime.now();
            user.setCreateDateTime(currentDate);
            user.setEncodedPassword(passwordEncoder.encode(user.getEncodedPassword()));
            userService.saveUser(user);
            String token = UUID.randomUUID().toString();
            ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), user);
            confirmationTokenService.saveConfirmationToken(confirmationToken);
            String link = "http://localhost:8080/api/users/confirm?token=" + token;
            emailService.send(user.getEmail(), registerService.buildEmail(user.getUsername(), link));
            URI location = fromCurrentRequest().path("/users/{id}")
                    .buildAndExpand(user.getId()).toUri();
            return ResponseEntity.created(location).body(user);
        }catch (Exception e){return new ResponseEntity<>(HttpStatus.BAD_REQUEST);}
    }
    @GetMapping("/users/confirm")
    public String confirm(@RequestParam("token") String token){
        return registerService.confirmToken(token);
    }



    //GET PERSONAL INFORMATION
    @Operation(summary = "Get user logged in app")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Source Found",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation= User.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Not Found",
                content = @Content
            )


    })
    @GetMapping("/users/me")
    public ResponseEntity<Optional<User>> getPersonalInfo(HttpServletRequest request){
        Principal principal = request.getUserPrincipal();
        if (principal!=null){
            return ResponseEntity.ok(userService.findByEmail(principal.getName()));
        }else
            return ResponseEntity.notFound().build();
    }


}
