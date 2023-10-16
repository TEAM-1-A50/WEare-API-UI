package test.cases.weareseleniumtests.tests;

import api.models.PostModel;
import com.telerikacademy.testframework.pages.weare.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import test.cases.weareseleniumtests.base.BaseWeareSeleniumTest;

public class SeleniumPostTest extends BaseWeareSeleniumTest {

    @Test
    // only text, default visibility private, no image
    public void user_Can_Create_Post_With_Valid_Input() {


        LoginPage loginPage = new LoginPage(actions.getDriver());
        loginPage.loginUser(username, password);

        String postMessage = helpers.generatePostContent();

        CreatePostPage createPostPage = new CreatePostPage(actions.getDriver());
        createPostPage.navigateToPage();
        createPostPage.createPost(postMessage);

        LatestPostsPage latestPostsPage = new LatestPostsPage(actions.getDriver());
        latestPostsPage.assertPageNavigated();
        latestPostsPage.assertPostIsCreated(postMessage);
    }

    @Test
    public void user_Can_Like_Post() {

        boolean publicVisibility = true;
        PostModel createdPost = this.WEareApi.createPost(user, publicVisibility);
        Integer postId = createdPost.getPostId();

        LoginPage loginPage = new LoginPage(actions.getDriver());
        loginPage.loginUser(username, password);

        LatestPostsPage latestPostsPage = new LatestPostsPage(actions.getDriver());
        latestPostsPage.navigateToPage();
        latestPostsPage.clickLikeButton(postId);

        latestPostsPage.assertPostIsLiked(postId);

        actions.clickElement("//a[text()=\"Home\"]");
        actions.clickElement("//a[text()=\"LOGOUT\"]");

    }

    @Test
    public void admin_User_Can_Edit_Another_Users_Post() {

        boolean publicVisibility = true;
        PostModel createdPost = this.WEareApi.createPost(user, publicVisibility);
        Integer postId = createdPost.getPostId();

        LoginPage loginPage = new LoginPage(actions.getDriver());
        loginPage.loginUser(adminUsername, adminPassword);

        EditPostPage editPostPage = new EditPostPage(actions.getDriver(), postId);
        editPostPage.navigateToPage();
        String message = "vse taq";
        editPostPage.editPostVisibility();
        editPostPage.editPostMessage(message);
        editPostPage.savePostChanges();

        PostPage postPage = new PostPage(actions.getDriver(), postId);
        Assert.assertTrue(postPage.messageIs(message), "Post message is not changed to " + message);
    }

    @Test
    public void admin_User_Can_Delete_Another_Users_Post() {

        boolean publicVisibility = true;
        PostModel createdPost = this.WEareApi.createPost(user, publicVisibility);
        Integer postId = createdPost.getPostId();

        LoginPage loginPage = new LoginPage(actions.getDriver());
        loginPage.loginUser(adminUsername, adminPassword);

        DeletePostPage deletePostPage = new DeletePostPage(actions.getDriver(), postId);
        deletePostPage.navigateToPage();
        deletePostPage.deletePost();

        Assert.assertTrue(actions.isElementPresent("weare.deletePostPage.deleteConfirmationMessage"),
                "Deletion confirmation is not present");

    }

}