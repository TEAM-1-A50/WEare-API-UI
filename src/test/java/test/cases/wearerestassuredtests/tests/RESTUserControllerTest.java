package test.cases.wearerestassuredtests.tests;

import api.models.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import test.cases.wearerestassuredtests.base.BaseWeareRestAssuredTest;

import static com.telerikacademy.testframework.utils.UserRoles.*;
import static org.testng.Assert.*;

public class RESTUserControllerTest extends BaseWeareRestAssuredTest {

    UserModel user;

    @BeforeClass
    public void setUpUserTest() {
        user = WEareApi.registerUser(ROLE_USER.toString());
    }

    @AfterClass
    public void cleanUpUserTest() {
        WEareApi.disableUser(globalRESTAdminUser, user.getId());
    }

    @Test
    public void UserRegistered_When_ValidCredentialsProvided() {

        UserModel userModel = WEareApi.registerUser(ROLE_USER.toString());

        assertEquals(userModel.getAuthorities().size(), 1, "User is not registered as \"USER\".");

    }

    @Test
    public void AdminUserRegistered_When_ValidCredentialsProvided() {

        UserModel adminUser = WEareApi.registerUser(ROLE_ADMIN.toString());

        assertEquals(adminUser.getUsername(), adminUser.getUsername(), "User was not registered");
        assertEquals(adminUser.getPassword(), adminUser.getPassword(), "User was not registered");
        assertEquals(adminUser.getAuthorities().size(), 2, "User was not registered as admin");

    }

    @Test
    public void UserPersonalProfileEdited_When_ValidDataProvided() {

        PersonalProfileModel personalProfile = WEareApi.editPersonalProfile(user);
        user.setPersonalProfile(personalProfile);

        assertEquals(user.getPersonalProfile(), personalProfile, "User personal profile was not updated.");

    }

    @Test
    public void UserExpertiseProfileEdited_When_ValidDataProvided() {

        ExpertiseProfileModel expertiseProfile = WEareApi.editExpertiseProfile(user);
        user.setExpertiseProfile(expertiseProfile);

        assertEquals(user.getExpertiseProfile(), expertiseProfile,
                "User expertise profile was not updated.");

    }

    @Test
    public void UserFound_When_SearchParametersProvided() {

        String firstname = user.getPersonalProfile().getFirstName();

        UserBySearchModel userAfterSearch = WEareApi.searchUser(user.getId(), firstname);

        assertEquals(userAfterSearch.getUsername(), user.getUsername(), "User was not found");
        assertEquals(userAfterSearch.getUserId(), user.getId(), "User was not found");

    }

    @Test
    public void UserPostsListed_When_Requested() {

        int postsCount = 3;
        for (int i = 0; i < postsCount; i++) {
            boolean publicVisibility = true;
            PostModel publicPost = WEareApi.createPost(user, publicVisibility);
            assertTrue(WEareApi.publicPostExists(publicPost.getPostId()), "Post not created.");
            publicVisibility = false;
            PostModel privatePost = WEareApi.createPost(user, publicVisibility);
            assertTrue(WEareApi.privatePostExists(user, privatePost.getPostId()), "Post not created.");
        }

        PostModel[] userPosts = WEareApi.showProfilePosts(user);

        assertEquals(userPosts.length, 2 * postsCount, "Wrong profile posts count");

        for (PostModel userPost : userPosts) {
            WEareApi.deletePost(user, userPost.getPostId());
            if (userPost.isPublic()) {
                assertFalse(WEareApi.publicPostExists(userPost.getPostId()), "Post not deleted.");
            } else {
                assertFalse(WEareApi.privatePostExists(user, userPost.getPostId()), "Post not deleted.");
            }
        }

    }

    @Test
    public void UserFoundById_When_Requested_By_AdminUser() {

        Response returnedUser = WEareApi.getUserById(globalRESTAdminUser.getUsername(), user.getId());

        int userId = Integer.parseInt(returnedUser.getBody().jsonPath().getString("id"));

        Assert.assertEquals(userId, user.getId(), "Ids do not match.");

    }

    @Test
    public void UserFoundById_When_Requested_By_AnotherUser() {

        UserModel userToFind = WEareApi.registerUser(ROLE_USER.toString());

        Response returnedUser = WEareApi.getUserById(user.getUsername(), userToFind.getId());

        int userId = Integer.parseInt(returnedUser.getBody().jsonPath().getString("id"));

        Assert.assertEquals(userId, userToFind.getId(), "Ids do not match.");

        WEareApi.disableUser(globalRESTAdminUser, userToFind.getId());

    }

    @Test
    public void UserDisabled_By_AdminUser() {

        UserModel userToBeDisabled = WEareApi.registerUser(ROLE_USER.toString());

        String firstname = userToBeDisabled.getPersonalProfile().getFirstName();

        assertTrue(userToBeDisabled.isEnabled(), "User is not enabled");

        WEareApi.disableUser(globalRESTAdminUser, userToBeDisabled.getId());

        UserBySearchModel returnedDisabledUser = WEareApi.searchUser(userToBeDisabled.getId(), firstname);

        assertEquals(returnedDisabledUser.getUserId(), userToBeDisabled.getId(), "Users do not match.");

        assertFalse(returnedDisabledUser.isEnabled(), "User was not disabled");
    }

    @Test
    public void UserEnabled_By_AdminUser() {

        UserModel userToBeEnabled = WEareApi.registerUser(ROLE_USER.toString());

        String firstname = userToBeEnabled.getPersonalProfile().getFirstName();

        WEareApi.disableUser(globalRESTAdminUser, userToBeEnabled.getId());

        UserBySearchModel returnedDisabledUser = WEareApi.searchUser(userToBeEnabled.getId(), firstname);

        assertEquals(returnedDisabledUser.getUserId(), userToBeEnabled.getId(), "Users do not match.");

        assertFalse(returnedDisabledUser.isEnabled(), "User is not disabled");

        WEareApi.enableUser(globalRESTAdminUser, userToBeEnabled);

        UserBySearchModel returnedEnabledUser = WEareApi.searchUser(userToBeEnabled.getId(), firstname);
        assertEquals(returnedEnabledUser.getUserId(), userToBeEnabled.getId(), "User ids do not match.");

        assertTrue(returnedEnabledUser.isEnabled(), "User wss not enabled");

        WEareApi.disableUser(globalRESTAdminUser, userToBeEnabled.getId());
    }

    // Delete User is not implemented and cannot be tested

}