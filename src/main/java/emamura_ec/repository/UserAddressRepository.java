package emamura_ec.repository;

import java.util.List;
import java.util.Optional;

import emamura_ec.entity.User;
import emamura_ec.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUserOrderByDefaultAddressDescUserAddressIdAsc(User user);

    Optional<UserAddress> findByUserAddressIdAndUser(Long userAddressId, User user);
}
