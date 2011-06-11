import unfiltered.request._
import unfiltered.response._
import unfiltered.jetty._
import unfiltered.filter.Planify

object Server extends App{
  val server = unfiltered.jetty.Http(8080).context("/static"){ ctx: ContextBuilder =>
    ctx.resources(new java.net.URL("file:///Users/molecule/development/scala/scalaexchange/static"))
  }
  
  server
    .filter(example1)
    .filter(example2)
    .filter(example3)
    .filter(example4)
    .filter(example5)
    .filter(example6)
    .filter(example7)
    .run
  
  lazy val example1 = Planify{
    case Path("/hello.html") => ResponseString("Hello, World!")
  }
  
  lazy val example2 = Planify{
    case Path("/unfiltered/example2.html") => 
      Ok ~> ContentType("application/json") ~> ResponseString((0 to 100).mkString("[",",","]"))
  }
  
  object IntOnly{
    def unapply(str: String): Option[Int] = {
      try{ Some(str.toInt)} catch { case _ => None}
    }
  }
  
  lazy val example3 = Planify{
    case GET(Path(Seg("unfiltered" :: "example3" :: IntOnly(num) :: Nil))) => ResponseString(num.toString)
  }
  
  lazy val example4 = Planify{
    case Path("/unfiltered/example4.html") & BasicAuth(username, "password") => 
      ResponseString("Welcome: %s" format username)
      
    case Path("/unfiltered/example4.html") =>
      WWWAuthenticate("Basic realm=\"Secure Area\"") ~> Unauthorized
  }
  
  lazy val example5 = Planify{
    case request @ Path("/unfiltered/example5.html") => 
      request match {
        case BasicAuth(username, "password5") => ResponseString("Welcome: %s" format username)
        case _ => WWWAuthenticate("Basic realm=\"Secure Area\"") ~> Unauthorized
      }   
  }
  
  object Number extends Params.Extract("number", Params.first ~> Params.int)
  
  lazy val example6 = Planify{
    
    case Path("/unfiltered/example6.html") & Params(Number(num)) => 
      ResponseString(num.toString)
    
    case Path("/unfiltered/example6.html") & Params(params) => 
      val json = (for(key <- params.keySet) yield {
        "\"%s\":%s".format(key, params(key).map("\"%s\"".format(_:String)).mkString("[",",", "]"))
      }).mkString("{", ",", "}")
      ContentType("application/json") ~> ResponseString(json)
  }
  
  lazy val example7 = Planify{
    case request @ Path("/unfiltered/example7.html") =>
      import QParams._
      val expected = for{
        username <- lookup("username") is(required("required"))
        password <- lookup("password") 
              .is(required("required")) 
              .is(pred( _ == "password", i => "incorrect password!"))
      } yield {
        ResponseString("Welcome " + username.get + " ")
      }
      
      val Params(params) = request
      expected(params) orFail{ fails =>
        BadRequest ~> ContentType("application/json") ~> ResponseString(
          fails.map({ fail => "{\"%s\":\"%s\"}".format(fail.name, fail.error)}).mkString("[",",", "]")
        )
      }
  }
}