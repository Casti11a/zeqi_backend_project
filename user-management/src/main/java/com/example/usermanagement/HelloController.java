package com.example.usermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class HelloController {

    @Autowired
    private UserRepository userRepository;
    private Map<String, Long> sessions = new HashMap<>();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user){
        User existing = userRepository.findByUsername(user.getUsername());
        if(existing != null){
            return ResponseEntity.status(409).body("此用户名已存在");
        }
        userRepository.save(user);
        return ResponseEntity.status(201).body(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody User loginUser) {
        User user = userRepository.findByUsername(loginUser.getUsername());
        if(user == null || !user.getPassword().equals(loginUser.getPassword())) {
            return "登陆失败，密码错误";
        }
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId,user.getId());
        return "登陆成功，你的sessionId是：" + sessionId;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("sessionId") String sessionId){
        Long userId = sessions.get(sessionId);
        if(userId == null) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userRepository.findById(userId).orElse(null);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    public List<User> list(){
        return userRepository.findAll();
    }

    @PutMapping("/users/{id}/username")
    public ResponseEntity<?> updateUsername(@PathVariable Long id,@RequestBody User updateUser){
        User user = userRepository.findById(id).orElse(null);
        if(user == null) {
            return ResponseEntity.status(404).body("用户不存在");
        }
        user.setUsername(updateUser.getUsername());
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable long id,@RequestBody User updateUser){
        User user = userRepository.findById(id).orElse(null);
        if(user == null) {
            return ResponseEntity.status(404).body("用户不存在");
        }
        user.setPassword(updateUser.getPassword());
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> delete(@PathVariable long id){
        if(!userRepository.existsById(id)) {
            return ResponseEntity.status(404).body("用户不存在");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("已注销");
    }
}