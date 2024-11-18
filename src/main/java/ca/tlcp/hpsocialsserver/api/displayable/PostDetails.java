package ca.tlcp.hpsocialsserver.api.displayable;

import ca.tlcp.hpsocialsserver.objects.Post;
import ca.tlcp.hpsocialsserver.objects.User;
import lombok.Data;

import java.util.List;

@Data
public class PostDetails {

    private long id;
    private String username;
    private String body;
    private List<Byte[]> attachedImages;

    public PostDetails(Post post) {
        this.username = post.getUser().getUsername();
        this.body = post.getBody();
        this.attachedImages = post.getAttachedImages();
        this.id = post.getId();
    }
}
