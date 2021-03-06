package controllers

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import jp.t2v.lab.play2.auth.test.Helpers._
import jp.t2v.lab.play2.pager._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserService
import java.security.MessageDigest
import java.math.BigInteger

import play.twirl.api.Html

class UsersControllerSpec extends PlayFunSpec with GuiceOneAppPerSuite {

  object Config extends AuthConfigSupport {
    override val userService: UserService = app.injector.instanceOf[UserService]
  }

  describe("UsersController") {
    describe("route of UserController#index") {
      it("should be valid when not logged in") {
        val result =
          route(app, addCsrfToken(FakeRequest(GET, routes.UsersController.index(Pager.default).toString))).get

        status(result) mustBe SEE_OTHER
      }

      it("should be valid when logged in") {
        val email = "test@test.com"
        val result =
          route(app,
                addCsrfToken(FakeRequest(GET, routes.UsersController.index(Pager.default).toString))
                  .withLoggedIn(Config)(email)).get

        val users = Config.userService.findAll(Pager.default)

        val userNames = users.get.items.map { user =>
          user.name
        }

        val userEmail = users.get.items.map(_.email)

        status(result) mustBe OK

        userNames.foreach { name =>
          contentAsString(result) must include(name)
        }

        userEmail.foreach { email: String =>
          val hash        = MessageDigest.getInstance("MD5").digest(email.getBytes("UTF-8"))
          val v           = new BigInteger(1, hash)
          val gravatarUrl = "https://secure.gravatar.com/avatar/" + v.toString(16)
          contentAsString(result) must include(gravatarUrl)
        }
      }
    }

    describe("route of UserController#show") {
      it("should be valid when not logged in") {
        val id = 1L
        val result =
          route(app, addCsrfToken(FakeRequest(GET, routes.UsersController.show(id).toString))).get

        status(result) mustBe SEE_OTHER
      }

      it("should be valid when logged in") {
        val email = "test@test.com"
        val id    = 1L
        val result =
          route(app,
                addCsrfToken(FakeRequest(GET, routes.UsersController.show(id).toString))
                  .withLoggedIn(Config)(email)).get

        val user        = Config.userService.findById(id)
        val hash        = MessageDigest.getInstance("MD5").digest(email.getBytes("UTF-8"))
        val v           = new BigInteger(1, hash)
        val gravatarUrl = "https://secure.gravatar.com/avatar/" + v.toString(16)

        status(result) mustBe OK
        contentAsString(result) must include(user.get.get.name)
        contentAsString(result) must include(gravatarUrl)

      }
    }
  }

}
