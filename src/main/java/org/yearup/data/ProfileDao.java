// src/main/java/org/yearup/data/ProfileDao.java
package org.yearup.data;

import org.yearup.models.Profile;

public interface ProfileDao
{
    /**
     * Create a blank profile (used right after user registration)
     * and return the newly inserted record.
     */
    Profile create(Profile profile);

    /** Fetch the profile for a given user ID. */
    Profile getByUserId(int userId);

    /** Update that user’s profile and return the updated record. */
    Profile update(int userId, Profile profile);
}
