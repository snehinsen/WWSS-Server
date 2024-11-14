package ca.tlcp.hpsocialsserver.api;

import ca.tlcp.hpsocialsserver.displayable.UserDetails;
import ca.tlcp.hpsocialsserver.objects.User;
import ca.tlcp.hpsocialsserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api")
public class APIController {

    @Autowired
    private UserRepository userRepository;

    private List<User> getFriends(User user) {
        List<User> friendsList = new ArrayList<>();
        for (long friendID : user.getFriends()) {
            User friend = userRepository.getUserById(friendID).get();
            friendsList.add(friend);
        }
        return friendsList;
    }

    @PostMapping(path = "/signup")
    public boolean addUser(
            @RequestParam String name,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String bio,
            @RequestParam boolean isWizarding,
            @RequestParam String handle
    ) {
        if (userRepository.existsUserByUsername(username)) {
            return false;
        } else {
            User tmpUser = new User();
            tmpUser.setName(name);
            tmpUser.setUsername(username);
            tmpUser.setPassword(Base64.getEncoder().encodeToString(password.getBytes()));
            tmpUser.setBot(false);
            tmpUser.setWizarding(isWizarding);
            tmpUser.setBio(bio);
            tmpUser.setHandle(handle);
            userRepository.save(tmpUser);
            return true;
        }
    }

    @GetMapping(path = "profile/{username}")
    public UserDetails getProfile(@PathVariable String username) {
        User selecteduser = userRepository.getUserByUsername(username).orElse(new User());
        List<User> friendsList = new ArrayList<>();
        if (selecteduser.getFriends() != null) {
            friendsList = getFriends(selecteduser);
        }
        UserDetails details = new UserDetails(selecteduser, friendsList);

        return details;
    }

    @GetMapping(path = "/users")
    public List<UserDetails> getAllUsers() {
        List<UserDetails> userDetailsList = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            List<User> friendsList = new ArrayList<>();
            if (user.getFriends() != null) {
                friendsList = getFriends(user);
                userDetailsList.add(new UserDetails(user, friendsList));
            } else {
                userDetailsList.add(new UserDetails(user, new ArrayList<User>()));
            }
        }
        return userDetailsList;
    }

}
