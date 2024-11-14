package ca.tlcp.hpsocialsserver.objects;

import jakarta.persistence.*;

import java.util.List;

public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String body8;
    private List<Byte[]> attachedImages;

    @OneToMany
    @JoinColumn(name = "user_id")
    private User user;
}
