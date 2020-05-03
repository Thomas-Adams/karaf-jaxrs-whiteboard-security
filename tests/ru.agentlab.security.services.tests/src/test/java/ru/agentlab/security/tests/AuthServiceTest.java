package ru.agentlab.security.tests;

import java.util.stream.Stream;

import javax.inject.Inject;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import com.nimbusds.oauth2.sdk.GrantType;

import ru.agentlab.security.oauth.service.IAuthService;
import ru.agentlab.security.tests.mock.wso2.Wso2TestConstants;
import ru.agentlab.security.tests.utils.FileUtil;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class AuthServiceTest extends SecurityJaxrsTestSupport {

    @Configuration
    public Option[] config() {
        Option[] options = new Option[] {
                // uncomment if you need to debug (blocks test execution and waits for the
                // debugger)
                // KarafDistributionOption.debugConfiguration("5005", true)
        };
        return Stream.of(super.config(), options).flatMap(Stream::of).toArray(Option[]::new);
    }

    @Inject
    private IAuthService authService;

    // Common tests

    @Test
    public void checkEmptyGrantType() {
        Form form = new Form();
        Response response = authService.grantOperation(form, null);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void checkMultipleGrantTypes() {
        Form form = new Form();
        form.param(Wso2TestConstants.GRANT_TYPE, GrantType.PASSWORD.getValue()).param(Wso2TestConstants.GRANT_TYPE,
                GrantType.REFRESH_TOKEN.getValue());

        Response response = authService.grantOperation(form, null);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    // Password tests

    @Test
    public void checkGrantTypeValidPassword() {

        Form form = new Form();
        form.param(Wso2TestConstants.GRANT_TYPE, GrantType.PASSWORD.getValue())
                .param(Wso2TestConstants.PASSWORD, Wso2TestConstants.TESTUSER_PASSWORD)
                .param(Wso2TestConstants.USERNAME, Wso2TestConstants.TESTUSER_USERNAME)
                .param(Wso2TestConstants.SCOPE, Wso2TestConstants.OPENID);

        Response response = authService.grantOperation(form, null);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(readFile(Wso2TestConstants.SUCCESS_TOKEN_RESPONSE_FILE), response.getEntity());
    }

    @Test
    public void checkGrantTypePasswordInvalidPassword() {
        Form form = new Form();
        form.param(Wso2TestConstants.GRANT_TYPE, GrantType.PASSWORD.getValue())
                .param(Wso2TestConstants.PASSWORD, Wso2TestConstants.TESTUSER_INVALID_PASSWORD)
                .param(Wso2TestConstants.USERNAME, Wso2TestConstants.TESTUSER_USERNAME)
                .param(Wso2TestConstants.SCOPE, Wso2TestConstants.OPENID);

        Response response = authService.grantOperation(form, null);

        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        Assert.assertEquals(readFile(Wso2TestConstants.AUTHENTICATION_FAILED_FOR_TESTUSER_FILE), response.getEntity());
    }

    // Refresh token tests

    @Test
    public void checkGrantTypeRefreshTokenActiveRefreshToken() {
        Form form = new Form();
        form.param(Wso2TestConstants.GRANT_TYPE, GrantType.REFRESH_TOKEN.getValue())
                .param(Wso2TestConstants.REFRESH_TOKEN, Wso2TestConstants.ACTIVE_REFRESH_TOKEN);

        Response response = authService.grantOperation(form, null);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(readFile(Wso2TestConstants.SUCCESS_TOKEN_RESPONSE_FILE), response.getEntity());
    }

    @Test
    public void checkGrantTypeRefreshTokenActiveRefreshTokenCookie() {
        Form form = new Form();
        form.param(Wso2TestConstants.GRANT_TYPE, GrantType.REFRESH_TOKEN.getValue());

        Response response = authService.grantOperation(form, Wso2TestConstants.ACTIVE_REFRESH_TOKEN);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(readFile(Wso2TestConstants.SUCCESS_TOKEN_RESPONSE_FILE), response.getEntity());
    }

    @Test
    public void checkGrantTypeRefreshTokenExpiredRefreshToken() {
        Form form = new Form();
        form.param(Wso2TestConstants.GRANT_TYPE, GrantType.REFRESH_TOKEN.getValue())
                .param(Wso2TestConstants.REFRESH_TOKEN, Wso2TestConstants.EXPIRED_ACESS_JWT_TOKEN);

        Response response = authService.grantOperation(form, null);

        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        Assert.assertEquals(readFile(Wso2TestConstants.REFRESH_TOKEN_EXPIRED_RESPONSE_FILE), response.getEntity());
    }

    @Test
    public void checkGrantTypeRefreshTokenExpiredRefreshTokenCookie() {
        Form form = new Form();
        form.param(Wso2TestConstants.GRANT_TYPE, GrantType.REFRESH_TOKEN.getValue());

        Response response = authService.grantOperation(form, Wso2TestConstants.EXPIRED_ACESS_JWT_TOKEN);

        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        Assert.assertEquals(readFile(Wso2TestConstants.REFRESH_TOKEN_EXPIRED_RESPONSE_FILE), response.getEntity());
    }

    @Test
    public void checkGrantTypeRefreshTokenEmptyRefreshToken() {
        Form form = new Form();
        form.param(Wso2TestConstants.GRANT_TYPE, GrantType.REFRESH_TOKEN.getValue());

        Response response = authService.grantOperation(form, null);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void checkGrantTypeRefreshTokenWithoutToken() {
        Form form = new Form();
        form.param(Wso2TestConstants.GRANT_TYPE, GrantType.REFRESH_TOKEN.getValue());

        Response response = authService.grantOperation(form, null);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void checkGrantTypeRefreshTokenWithActiveFormTokenAndActiveCookieToken() {
        Form form = new Form();
        form.param(Wso2TestConstants.GRANT_TYPE, GrantType.REFRESH_TOKEN.getValue())
                .param(Wso2TestConstants.REFRESH_TOKEN, Wso2TestConstants.ACTIVE_REFRESH_TOKEN);

        Response response = authService.grantOperation(form, Wso2TestConstants.ACTIVE_REFRESH_TOKEN);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(readFile(Wso2TestConstants.SUCCESS_TOKEN_RESPONSE_FILE), response.getEntity());
    }

    @Test
    public void checkGrantTypeRefreshTokenWithExpiredFormTokenAndActiveCookieToken() {
        Form form = new Form();
        form.param(Wso2TestConstants.GRANT_TYPE, GrantType.REFRESH_TOKEN.getValue())
                .param(Wso2TestConstants.REFRESH_TOKEN, Wso2TestConstants.EXPIRED_REFRESH_TOKEN);

        Response response = authService.grantOperation(form, Wso2TestConstants.ACTIVE_REFRESH_TOKEN);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(readFile(Wso2TestConstants.SUCCESS_TOKEN_RESPONSE_FILE), response.getEntity());
    }

    @Test
    public void checkGrantTypeRefreshTokenWithActiveFormTokenAndExpiredCookieToken() {
        Form form = new Form();
        form.param(Wso2TestConstants.GRANT_TYPE, GrantType.REFRESH_TOKEN.getValue())
                .param(Wso2TestConstants.REFRESH_TOKEN, Wso2TestConstants.ACTIVE_REFRESH_TOKEN);

        Response response = authService.grantOperation(form, Wso2TestConstants.EXPIRED_ACESS_JWT_TOKEN);

        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        Assert.assertEquals(readFile(Wso2TestConstants.REFRESH_TOKEN_EXPIRED_RESPONSE_FILE), response.getEntity());
    }

    @Test
    public void checkGrantTypeRefreshTokenWithExpiredFormTokenAndExpiredCookieToken() {
        Form form = new Form();
        form.param(Wso2TestConstants.GRANT_TYPE, GrantType.REFRESH_TOKEN.getValue())
                .param(Wso2TestConstants.REFRESH_TOKEN, Wso2TestConstants.EXPIRED_REFRESH_TOKEN);

        Response response = authService.grantOperation(form, Wso2TestConstants.EXPIRED_REFRESH_TOKEN);

        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        Assert.assertEquals(readFile(Wso2TestConstants.REFRESH_TOKEN_EXPIRED_RESPONSE_FILE), response.getEntity());
    }

    // Userinfo tests

    @Test
    public void checkUserInfoNoToken() {

        Response response = authService.userInfo(null, null);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void checkUserInfoValidTokenCookie() {

        Response response = authService.userInfo(null, Wso2TestConstants.VALID_ACCESS_JWT_TOKEN);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(readFile(Wso2TestConstants.USERINFO_SUCCESS_FILE), response.getEntity());
    }

    @Test
    public void checkUserInfoExpiredTokenCookie() {

        Response response = authService.userInfo(null, Wso2TestConstants.EXPIRED_ACESS_JWT_TOKEN);

        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void checkUserInfoHackedTokenCookie() {

        Response response = authService.userInfo(null, Wso2TestConstants.HACKED_ACCESS_JWT_TOKEN);

        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void checkUserInfoNoJwtTokenCookie() {

        Response response = authService.userInfo(null, Wso2TestConstants.NON_JWT_ACCESS_TOKEN);

        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void checkUserInfoValidToken() {

        Response response = authService.userInfo(getBearerHeaderValue(Wso2TestConstants.VALID_ACCESS_JWT_TOKEN), null);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(readFile(Wso2TestConstants.USERINFO_SUCCESS_FILE), response.getEntity());
    }

    @Test
    public void checkUserInfoExpiredToken() {

        Response response = authService.userInfo(getBearerHeaderValue(Wso2TestConstants.EXPIRED_ACESS_JWT_TOKEN), null);

        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void checkUserInfoHackedToken() {

        Response response = authService.userInfo(getBearerHeaderValue(Wso2TestConstants.HACKED_ACCESS_JWT_TOKEN), null);

        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void checkUserInfoNoJwtToken() {

        Response response = authService.userInfo(getBearerHeaderValue(Wso2TestConstants.NON_JWT_ACCESS_TOKEN), null);

        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void checkUserInfoNoNBearerJwtToken() {

        Response response = authService.userInfo(Wso2TestConstants.NON_JWT_ACCESS_TOKEN, null);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void checkUserInfoPriorityOfHeaderTokenAndCookie() {

        Response response = authService.userInfo(getBearerHeaderValue(Wso2TestConstants.EXPIRED_ACESS_JWT_TOKEN),
                Wso2TestConstants.VALID_ACCESS_JWT_TOKEN);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(readFile(Wso2TestConstants.USERINFO_SUCCESS_FILE), response.getEntity());
    }

    private String getBearerHeaderValue(String token) {
        return "Bearer " + token;
    }

    private String readFile(String fileName) {
        return FileUtil.readFile(this.getClass().getClassLoader().getResource(fileName));
    }

}
