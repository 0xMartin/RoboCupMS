package com.robogames.RoboCupMS.Business.Security;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Base64.Encoder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.AppInit;
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
public class TokenAuthorization extends OncePerRequestFilter {

	private final String PREFIX = "Bearer";

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
	public TokenAuthorization(String _x_token, UserRepository _repository, String[] _ignoredEndpoints) {
		this.x_token = _x_token;
		this.repository = _repository;
		this.ignoredEndpoints = _ignoredEndpoints;
	}

	// Konstanty pro rozliseni duvodu chyby autentizace
	private static final String ERROR_TOKEN_MISSING = "TOKEN_MISSING";
	private static final String ERROR_TOKEN_EXPIRED = "TOKEN_EXPIRED";
	private static final String ERROR_TOKEN_INVALID = "TOKEN_INVALID";
	private static final String ERROR_NO_ROLE = "NO_ROLE";

	// Flag pro rozliseni expirovaneho tokenu
	private boolean tokenExpired = false;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		// endpoint filter
		if (this.ignoredEndpoints != null) {
			final String uri = request.getRequestURI();
			for (String ep : this.ignoredEndpoints) {
				if (ep.equals(uri)) {
					chain.doFilter(request, response);
					return;
				}
			}
		}

		// reset flag
		this.tokenExpired = false;

		// validace tokenu
		String errorCode;
		String msg;
		int httpStatus;
		UserRC user = null;

		if ((user = validateToken(request)) != null) {
			if (setUpSpringAuthentication(user, request.getHeader(this.x_token))) {
				chain.doFilter(request, response);
				return;
			}
			// Uzivatel nema zadnou roli - 403 Forbidden (je prihlasen, ale nema opravneni)
			errorCode = ERROR_NO_ROLE;
			msg = "You have no role";
			httpStatus = HttpServletResponse.SC_FORBIDDEN;
		} else {
			// Token je neplatny - zjistime proc
			String accessToken = request.getHeader(this.x_token);
			if (accessToken == null || accessToken.isEmpty()) {
				errorCode = ERROR_TOKEN_MISSING;
				msg = "Access token is missing";
				httpStatus = HttpServletResponse.SC_UNAUTHORIZED;
			} else if (this.tokenExpired) {
				errorCode = ERROR_TOKEN_EXPIRED;
				msg = "Access token has expired";
				httpStatus = HttpServletResponse.SC_UNAUTHORIZED;
			} else {
				errorCode = ERROR_TOKEN_INVALID;
				msg = "Access token is invalid";
				httpStatus = HttpServletResponse.SC_UNAUTHORIZED;
			}
		}

		// pristup zamitnut
		SecurityContextHolder.clearContext();
		response.setStatus(httpStatus);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		ServletOutputStream outputStream = response.getOutputStream();
		// Pridame errorCode do odpovedi pro frontend
		outputStream.println("{\"type\":\"ERROR\",\"errorCode\":\"" + errorCode + "\",\"data\":\"" + msg + "\"}");
		outputStream.flush();
	}

	/**
	 * Autentizace uzivatele
	 * 
	 * @param user Uzivatel ktery zada system o autentizaci
	 */
	private boolean setUpSpringAuthentication(UserRC user, String token) {
		// pokud uzivatel nema zadnou roli, nemuze pristoupit
		if (user.getRoles().isEmpty()) {
			return false;
		}

		// Set roly uzivatele prevede na kolekci SimpleGrantedAuthority
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		for (Role r : user.getRoles()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + r.getName().toString()));
		}

		// Nastaveni spring security
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, token,
				authorities);
		SecurityContextHolder.getContext().setAuthentication(auth);

		return true;
	}

	/**
	 * Validuje token a nejde v databazi uzivatele, kteremu nalezi a chce
	 * pristupovat k endpointu vyzadujicimu autorizaci (uzivatel musi byt prihlasen
	 * => jeho TOKEN je zapsan v databazi).
	 * Token se stava automaticky neplatnym po uplynuti
	 * definovaneho casu "GlobalConfig.TOKEN_VALIDITY_DURATION"
	 * 
	 * @param request HttpServletRequest
	 * @return UserRC
	 */
	private UserRC validateToken(HttpServletRequest request) {
		if (request == null) {
			return null;
		}

		// pristopovy token
		String accessToken = request.getHeader(this.x_token);

		// token neni definovan
		if (accessToken == null) {
			return null;
		}
		if (accessToken.length() == 0) {
			return null;
		}

		// prefix check
		accessToken = accessToken.trim();
		if (!accessToken.startsWith(PREFIX)) {
			return null;
		}
		accessToken = accessToken.replace(PREFIX, "").trim();

		// najde uzivatele podle pristupoveho tokenu
		Optional<UserRC> user = this.repository.findByToken(accessToken);
		if (!user.isPresent()) {
			return null;
		}
		// overi casovou platnost
		Date now = new java.util.Date(Calendar.getInstance().getTime().getTime());
		long diff = now.getTime() - user.get().getLastAccessTime().getTime();
		if (user.get().getLastAccessTime() != null) {
			if (diff / (60 * 1000) > GlobalConfig.TOKEN_VALIDITY_DURATION) {
				// Token expiroval - odhlasime uzivatele i z Keycloaku
				logoutUserFromKeycloak(user.get());

				user.get().setToken(null);
				this.repository.save(user.get());
				// Oznacime ze token expiroval (pro lepsi error message)
				this.tokenExpired = true;
				return null;
			}
		}

		// aktualizuje cas posledniho pristupu, pokud uplynul definovany interval
		if (diff > ((long) GlobalConfig.TOKEN_REFRESH_SAVE_INTERVAL_SECONDS * 1000L)) {
			user.get().setLastAccessTime(now);
			this.repository.save(user.get());
		}

		return user.get();
	}

	/**
	 * Vygeneruje pristupovy token pro uzivatele
	 * 
	 * @param _user       Uzivatel, pro ktereho se ma vygenerovat token
	 * @param _repository Repozitar uzivatelu
	 * @return Pristupovy token
	 * @throws Exception
	 */
	public static String generateAccessTokenForUser(UserRC _user, UserRepository _repository) throws Exception {
		if (_user == null) {
			throw new Exception("failure, user is null");
		}
		if (_repository == null) {
			throw new Exception("failure, user repository is null");
		}

		// vygenerovani unikatniho pristupoveho tokenu
		String token = "";
		boolean success = false;
		for (int i = 0; i < 1000; ++i) {
			token = TokenAuthorization.generateToken();
			if (!_repository.findByToken(token).isPresent()) {
				success = true;
				break;
			}
		}

		// nepodarilo se vygenerovat pristupovy token
		if (!success) {
			throw new Exception("failed to generate access token");
		}

		// ulozi token a cas do databaze
		_user.setToken(token);
		_user.setLastAccessTime(new java.util.Date(Calendar.getInstance().getTime().getTime()));
		_repository.save(_user);
		return token;
	}

	private static final SecureRandom secureRandom = new SecureRandom();
	private static final Encoder base64Encoder = Base64.getUrlEncoder();

	/**
	 * Nahodne vygeneruje token
	 * 
	 * @return Novy teoken
	 */
	public static String generateToken() {
		byte bytes[] = new byte[64];
		secureRandom.nextBytes(bytes);
		return base64Encoder.encodeToString(bytes);
	}

	/**
	 * Odhlasi uzivatele z Keycloaku pri expiraci tokenu
	 * 
	 * @param user Uzivatel k odhlaseni
	 */
	private void logoutUserFromKeycloak(UserRC user) {
		try {
			AuthService authService = (AuthService) AppInit.contextProvider()
					.getApplicationContext()
					.getBean("authService");
			authService.logoutFromKeycloak(user);
		} catch (Exception e) {
			System.err.println("Failed to get AuthService for Keycloak logout: " + e.getMessage());
		}
	}

}