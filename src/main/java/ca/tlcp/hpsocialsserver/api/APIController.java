package ca.tlcp.hpsocialsserver.api;

import ca.tlcp.hpsocialsserver.api.displayable.CommentDetails;
import ca.tlcp.hpsocialsserver.api.displayable.PostDetails;
import ca.tlcp.hpsocialsserver.api.displayable.UserDetails;
import ca.tlcp.hpsocialsserver.objects.Comment;
import ca.tlcp.hpsocialsserver.objects.Post;
import ca.tlcp.hpsocialsserver.objects.User;
import ca.tlcp.hpsocialsserver.repositories.CommentRepository;
import ca.tlcp.hpsocialsserver.repositories.PostRepository;
import ca.tlcp.hpsocialsserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class APIController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

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
//        addTMPPost();
        return userDetailsList;
    }
    @GetMapping(path = "feed")
    public List<PostDetails> feed() {
        List<PostDetails> postDetails = new ArrayList<>();
        for (Post post : postRepository.findAll()) {
            postDetails.add(new PostDetails(post));
        }
        return postDetails;
    }

    @GetMapping(path = "getPost")
    public PostDetails getPost(@RequestParam(name = "id") long id) {
        System.out.println("ID: " + id);
        return new PostDetails(postRepository.findById(id).get());
    }

    @PostMapping(path = "addPost")
    public boolean addPost(
            @RequestParam String body,
            @RequestParam String username
    ) {
        System.out.println(body);
        System.out.println(username);
        postRepository.save(
                new Post(body, null, userRepository.getUserByUsername(username).get())
        );
        return true;
    }

    @GetMapping(path = "getPostComments/{pID}")
    public List<CommentDetails> getComments(@PathVariable long pID) {
        List<CommentDetails> details = new ArrayList<>();
        try{
            for (Comment comment : commentRepository
                    .getAllByPost(
                            postRepository
                                    .findById(pID)
                                    .get())) {
                details.add(new CommentDetails(comment));
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        return details;

    }

    private void addTMPPost() {
        User user = userRepository.getUserByUsername("harrypotter").get();
        Post post = new Post();
        post.setBody("Hello Everyone! How's life everything good? The Minustry finnally chucked Umbridge in prison");
        post.setUser(user);
        postRepository.save(post);
        System.out.println("Temp post added.");
    }
}
