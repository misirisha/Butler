package org.emerald.butler.screen.user;

import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.emerald.butler.entity.User;

@UiController("User.browse")
@UiDescriptor("user-browse.xml")
@LookupComponent("usersTable")
@Route("users")
public class UserBrowse extends StandardLookup<User> {
}