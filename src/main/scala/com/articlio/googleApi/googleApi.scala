//
// Raw implementation not tried out. 
// Must run from a cloud server as the api requires a public callback url.
//
// For further work consult when necessary:
//  https://developers.google.com/accounts/docs/OAuth2
//  https://developers.google.com/google-apps/spreadsheets/#setting_up_your_client_library
//  http://stackoverflow.com/questions/27572617/oauth2-authorization-from-java-scala-using-google-gdata-client-api
//  http://stackoverflow.com/questions/19361814/create-spreadsheet-using-google-spreadsheet-api-in-google-drive-java?rq=1
//  http://blog.knoldus.com/2013/07/30/integrating-google-drive-infrastructure-in-play-scala-application/
//  http://blog.knoldus.com/2014/03/29/create-upload-delete-google-docs-using-play-scala-application/
//  EC2 integration: http://stackoverflow.com/questions/14106962/google-places-api-request-denied-when-using-server-side-proxy-on-ec2
//

package com.articlio.googleApi
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthRsaSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthSigner;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed
import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.BaseFeed;
import com.google.gdata.data.Feed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

//remove if not needed
import scala.collection.JavaConversions._

object OAuth2Sample {

  def loginOAuth2(clientID: String, clientSecret: String) {
    val SCOPES = "https://docs.google.com/feeds https://spreadsheets.google.com/feeds"
    val oauthParameters = new GoogleOAuthParameters
    oauthParameters.setOAuthConsumerKey(clientID) //
    var signer: OAuthSigner = null
    oauthParameters.setOAuthConsumerSecret(clientSecret) //
    signer = new OAuthHmacSha1Signer()
    val oauthHelper = new GoogleOAuthHelper(signer)
    oauthParameters.setScope(SCOPES)
    try {
      oauthHelper.getUnauthorizedRequestToken(oauthParameters)
    } catch {
      case e: OAuthException => e.printStackTrace()
    }
    val requestUrl = oauthHelper.createUserAuthorizationUrl(oauthParameters)
    println(requestUrl)
    println("Please visit the URL above to authorize your OAuth " + 
      "request token.  Once that is complete, press any key to " + 
      "continue...")
    try {
      System.in.read()
    } catch {
      case e: IOException => e.printStackTrace()
    }
    var token: String = null
    try {
      token = oauthHelper.getAccessToken(oauthParameters)
    } catch {
      case e: OAuthException => e.printStackTrace()
    }
    println("OAuth Access Token: " + token)
    println()
    var feedUrl: URL = null
    try {
      feedUrl = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full")
    } catch {
      case e: MalformedURLException => e.printStackTrace()
    }
    println("Sending request to " + feedUrl.toString)
    println()
    val googleService = new SpreadsheetService("oauth-sample-app")
    try {
      googleService.setOAuthCredentials(oauthParameters, signer)
    } catch {
      case e: OAuthException => e.printStackTrace()
    }
    val feed = googleService.getFeed(feedUrl, classOf[SpreadsheetFeed])
    val spreadsheets = feed.getEntries
    println("Response Data:")
    println("=====================================================")
    if (spreadsheets != null) {
      for (spreadsheet <- spreadsheets) {
        println(spreadsheet.getTitle.getPlainText)
      }
    }
    println("=====================================================")
    println()
    println("Revoking OAuth Token...")
    try {
      oauthHelper.revokeToken(oauthParameters)
    } catch {
      case e: OAuthException => e.printStackTrace()
    }
    println("OAuth Token revoked...")
  }
}

