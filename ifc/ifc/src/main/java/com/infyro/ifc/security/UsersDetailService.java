package com.infyro.ifc.security;

import com.infyro.ifc.exception.UserNotFoundException;
import com.infyro.ifc.model.User;
import com.infyro.ifc.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class UsersDetailService implements UserDetailsService {

    private final UserRepo usersRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = usersRepo.findUserByEmail((email)).
                orElseThrow(()-> new UserNotFoundException(
                        "User not found with this email id : " + email));
        return new UserPrincipal(user);
    }
}
