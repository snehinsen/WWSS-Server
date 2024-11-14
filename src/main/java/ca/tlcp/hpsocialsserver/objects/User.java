package ca.tlcp.hpsocialsserver.objects;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private String name;
    private String bio;
    private byte[] pfp;
    @Column(nullable = false)
    private boolean isBot;
    @Column(nullable = false)
    private boolean isWizarding;

    @Column(unique = true, nullable = false)
    private String username;
    private String password;

    private long[] friends;
    @Column(unique = true, nullable = false)
    private String handle;

    public User(String name, String bio, byte[] pfp, boolean isBot, boolean isWizarding, String username, String password) {
        this.name = name;
        this.bio = bio;
        this.pfp = pfp;
        this.isBot = isBot;
        this.isWizarding = isWizarding;
        this.username = username;
        this.password = password;
    }
}
