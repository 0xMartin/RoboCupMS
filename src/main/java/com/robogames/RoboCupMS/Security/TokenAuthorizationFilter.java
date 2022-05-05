package com.robogames.RoboCupMS.Security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.robogames.RoboCupMS.ResponseHandler;
import com.robogames.RoboCupMS.Entity.Role;
import com.robogames.RoboCupMS.Entity.UserRC;
import com.robogames.RoboCupMS.Repository.UserRepository;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter pro autorizaci uzivatelu (pomoci tokenu)
 */
public class TokenAuthorizationFilter extends OncePerRequestFilter {

	private final String x_token;

	private final UserRepository repository;

	private final String[] ignoredEndpoints;

	/**
	 * Vytvori token filter
	 * 
	 * @param _x_token          Nazev fieldu v headeru requestu, ktery obsahuje
	 *                          pristupovy token
	 * @param _repository       Repozitar z uzivately
	 * @param _ignoredEndpoints Endpointy, ktery bude filter ignorovat
	 */
	public TokenAuthorizationFilter(String _x_token, UserRepository _repository, String[] _ignoredEndpoints) {
		this.x_token = _x_token;
		this.repository = _repository;
		this.ignoredEndpoints = _ignoredEndpoints;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		// endpoint filter
		if (this.ignoredEndpoints != null) {
			String uri = request.getRequestURI();
			for (String ep : this.ignoredEndpoints) {
				if (ep.equals(uri)) {
					chain.doFilter(request, response);
					return;
				}
			}
		}

		// validace tokenu
		UserRC user = null;
		if ((user = validateToken(request)) != null) {
			setUpSpringAuthentication(user, request.getHeader(this.x_token));
		} else {
			SecurityContextHolder.clearContext();
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			ServletOutputStream outputStream = response.getOutputStream();
			outputStream.println(ResponseHandler.error("Access token is invalid").toString());
			outputStream.flush();
		}

		chain.doFilter(request, response);
	}

	/**
	 * Autentizace uzivatele
	 * 
	 * @param user Uzivatel ktery zada system o autentizaci
	 */
	private void setUpSpringAuthentication(UserRC user, String token) {
		// Set roly uzivatele prevede na kolekci SimpleGrantedAuthority
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		for (Role r : user.getRoles()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + r.getName().toString()));
		}

		// Nastaveni spring security
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, token,
				authorities);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	/**
	 * Validuje token a nejde v databazi uzivatele, kteremu nalezi a chce
	 * pristupovat k chranemu endpointu (uzivatel musi byt prihlasen => jeho TOKEN
	 * je zapsan v databazi)
	 * 
	 * @param request HttpServletRequest
	 * @return UserRC
	 */
	private UserRC validateToken(HttpServletRequest request) {
		if (request == null) {
			return null;
		}

		String accessToken = request.getHeader(this.x_token);

		if (accessToken == null) {
			return null;
		}
		if (accessToken.length() == 0) {
			return null;
		}

		Optional<UserRC> user = this.repository.findByToken(accessToken);
		if (user.isPresent()) {
			return user.get();
		}

		return null;
	}

}