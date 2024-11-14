package ca.tlcp.hpsocialsserver.displayable;

import ca.tlcp.hpsocialsserver.objects.User;
import ca.tlcp.hpsocialsserver.repositories.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class UserDetails {

    private String name;
    private String bio;
    private byte[] pfp;
    private List<User> friends;
    private String handle;

    public UserDetails(User user, List<User> friendsList) {
        friends = new ArrayList<>();
        name = user.getName();
        bio = user.getBio();
        pfp = user.getPfp();
        friends = friendsList;
        handle = user.getHandle();

    }

    @Override
    public String toString() {
        return "{" +
                "\"name\": \"" + name + "\"," +
                "\"bio\": \"" + bio + "\"," +
                "\"pfp\": \"" + Arrays.toString(pfp) + "\"," +
                "\"friends\": " + friends +
                "\"handle\": " + handle + "," +
                "}";
    }
}
