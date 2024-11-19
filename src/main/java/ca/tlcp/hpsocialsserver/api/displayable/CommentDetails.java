package ca.tlcp.hpsocialsserver.api.displayable;

import ca.tlcp.hpsocialsserver.objects.Comment;
import ca.tlcp.hpsocialsserver.objects.Post;
import lombok.Data;

import java.util.List;

@Data
public class CommentDetails {

    private long id;
    private String username;
    private String body;
    private List<Byte[]> attachedImages;

    public CommentDetails(Comment comment) {
        this.username = comment.getPost().getUser().getUsername();
        this.body = comment.getBody();
        this.attachedImages = comment.getAttachedImages();
        this.id = comment.getId();
    }
}
