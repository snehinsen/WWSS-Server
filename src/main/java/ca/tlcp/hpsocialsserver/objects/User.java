package ca.tlcp.hpsocialsserver.objects;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String bio;
    private byte[] pfp;

}
