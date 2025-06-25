package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;
import org.yearup.models.User;

@RestController
@RequestMapping("/profile")
@CrossOrigin
public class ProfileController {

    private final ProfileDao profileDao;
    private final UserDao    userDao;

    @Autowired
    public ProfileController(ProfileDao profileDao, UserDao userDao) {
        this.profileDao = profileDao;
        this.userDao    = userDao;
    }

    // GET /profile
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public Profile getProfile(Authentication auth) {
        User user = userDao.getByUserName(auth.getName());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        Profile profile = profileDao.getByUserId(user.getId());
        if (profile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
        }
        return profile;
    }

    // PUT /profile
    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public Profile updateProfile(Authentication auth,
                                 @RequestBody Profile updated) {
        User user = userDao.getByUserName(auth.getName());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        updated.setUserId(user.getId());
        return profileDao.update(user.getId(), updated);
    }
}
