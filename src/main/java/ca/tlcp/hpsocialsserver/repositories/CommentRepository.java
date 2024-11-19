package ca.tlcp.hpsocialsserver.repositories;

import ca.tlcp.hpsocialsserver.objects.Comment;
import ca.tlcp.hpsocialsserver.objects.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> getAllByPost(Post post);

}
