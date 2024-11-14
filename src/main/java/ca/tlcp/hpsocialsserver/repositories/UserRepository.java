package ca.tlcp.hpsocialsserver.repositories;

import ca.tlcp.hpsocialsserver.objects.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> getUserByUsername(String username);
    Optional<User> getUserById(long id);
    Optional<User> getUserByHandle(String handle);
    boolean existsUserByUsername(String username);
}
