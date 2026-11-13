package se.yrgo.libraryapp.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.jooby.Context;
import io.jooby.Cookie;
import io.jooby.StatusCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pac4j.core.exception.CredentialsException;
import se.yrgo.libraryapp.dao.RoleDao;
import se.yrgo.libraryapp.dao.SessionDao;
import se.yrgo.libraryapp.entities.Role;
import se.yrgo.libraryapp.entities.UserId;
import se.yrgo.libraryapp.entities.forms.LoginData;
import se.yrgo.libraryapp.services.UserService;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private RoleDao roleDao;

    @Mock
    private SessionDao sessionDao;

    @Mock
    private Context context;

    @Test
    void loginWithValidCredentialsCreatesSessionAndReturnsRoles() {
        UserId userId = UserId.of(1);
        UUID sessionId = UUID.randomUUID();
        List<Role> roles = List.of(Role.USER);

        when(userService.validate("testuser", "password"))
                .thenReturn(Optional.of(userId));
        when(sessionDao.create(userId)).thenReturn(sessionId);
        when(roleDao.get(userId)).thenReturn(roles);

        LoginController controller = new LoginController(userService, roleDao, sessionDao);

        assertThat(controller.login(context, null, new LoginData("testuser", "password")))
                .isEqualTo(roles);

        verify(sessionDao).create(userId);
        verify(context).setResponseCookie(any(Cookie.class));
        verify(roleDao).get(userId);
    }

    @Test
    void loginWithInvalidCredentialsReturnsEmptyAndSetsUnauthorized() {
        when(userService.validate("testuser", "wrong-password"))
                .thenReturn(Optional.empty());

        LoginController controller = new LoginController(userService, roleDao, sessionDao);

        assertThat(controller.login(
                context, null, new LoginData("testuser", "wrong-password"))).isEmpty();

        verify(context).setResponseCode(StatusCode.UNAUTHORIZED);
        verify(sessionDao, never()).create(any());
        verifyNoInteractions(roleDao);
    }

    @Test
    void loginWithAlreadyValidSessionDoesNotAttemptLoginAgain() {
        UUID sessionId = UUID.randomUUID();
        UserId userId = UserId.of(1);
        when(sessionDao.validate(sessionId)).thenReturn(userId);

        LoginController controller = new LoginController(userService, roleDao, sessionDao);

        assertThat(controller.login(
                context, sessionId.toString(), new LoginData("otheruser", "password")))
                .isEmpty();

        verifyNoInteractions(userService, roleDao);
        verify(sessionDao).validate(sessionId);
        verify(sessionDao, never()).create(any());
    }

    @Test
    void isLoggedInWithValidSessionReturnsRoles() {
        UUID sessionId = UUID.randomUUID();
        UserId userId = UserId.of(1);
        List<Role> roles = List.of(Role.ADMIN);

        when(sessionDao.validate(sessionId)).thenReturn(userId);
        when(roleDao.get(userId)).thenReturn(roles);

        LoginController controller = new LoginController(userService, roleDao, sessionDao);

        assertThat(controller.isLoggedIn(sessionId.toString())).isEqualTo(roles);

        verify(sessionDao).validate(sessionId);
        verify(roleDao).get(userId);
    }

    @Test
    void isLoggedInWithoutSessionReturnsEmpty() {
        LoginController controller = new LoginController(userService, roleDao, sessionDao);

        assertThat(controller.isLoggedIn(null)).isEmpty();

        verifyNoInteractions(userService, roleDao, sessionDao);
    }

    @Test
    void isLoggedInWithMalformedSessionCookieReturnsEmpty() {
        LoginController controller = new LoginController(userService, roleDao, sessionDao);

        assertThat(controller.isLoggedIn("not-a-uuid")).isEmpty();

        verifyNoInteractions(userService, roleDao, sessionDao);
    }

    @Test
    void isLoggedInWhenSessionIsRejectedReturnsEmpty() {
        UUID sessionId = UUID.randomUUID();
        when(sessionDao.validate(sessionId))
                .thenThrow(new CredentialsException("Session is invalid"));

        LoginController controller = new LoginController(userService, roleDao, sessionDao);

        assertThat(controller.isLoggedIn(sessionId.toString())).isEmpty();

        verify(sessionDao).validate(sessionId);
        verifyNoInteractions(userService, roleDao);
    }
}