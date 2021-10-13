package io.agriledger.webserver.security

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import io.agriledger.webserver.models.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class TokenFilter : OncePerRequestFilter() {
    private val acceptableURLs = listOf( Regex(".*/swagger-resources.*")
            ,Regex( ".*/swagger-ui\\.html.*")
            ,Regex( ".*/v2/api-docs.*")
            ,Regex( ".*/configuration/ui.*")
            ,Regex( ".*/configuration/security.*")
            ,Regex( ".*/webjars.*")
            ,Regex( ".*/csrf.*")
    )

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        if(acceptableURLs.any{it.matches(request.requestURI)}){
            var user = User(
                            uid = "anonymous",
                            email = "anonymous"
                            )
            val authentication = UsernamePasswordAuthenticationToken(
            user, null, null)
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
        }
        else{
            val idToken: String = getTokenFromRequest(request)
            var decodedToken: FirebaseToken? = null
            try {
                decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
            }
            catch (e: FirebaseAuthException) {
                //log.error("Firebase Exception:: ", e.getLocalizedMessage());
            }
            if (decodedToken != null) {
                var user = User(
                        uid = decodedToken.uid,
                        email = decodedToken.email ?: decodedToken.claims["phone_number"].toString()
                )
                val authentication = UsernamePasswordAuthenticationToken(
                        user, decodedToken, null)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }
        }
        filterChain.doFilter(request, response)
    }


    open fun getTokenFromRequest(request: HttpServletRequest): String{
        var token: String? = null;
        val bearerToken: String = request.getHeader("Authorization");
        if (bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7, bearerToken.length);
        }
        token = token ?: ""
        return token
    }

}