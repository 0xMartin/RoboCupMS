// package com.robogames.RoboCupMS.Business.Security;

// import java.io.IOException;
// import java.security.SecureRandom;
// import java.util.ArrayList;
// import java.util.Base64;
// import java.util.Calendar;
// import java.util.Date;
// import java.util.List;
// import java.util.Optional;
// import java.util.Base64.Encoder;

// import javax.servlet.FilterChain;
// import javax.servlet.ServletException;
// import javax.servlet.ServletOutputStream;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;

// import com.robogames.RoboCupMS.GlobalConfig;
// import com.robogames.RoboCupMS.ResponseHandler;
// import com.robogames.RoboCupMS.Entity.Role;
// import com.robogames.RoboCupMS.Entity.UserRC;
// import com.robogames.RoboCupMS.Repository.UserRepository;

// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.web.filter.OncePerRequestFilter;

// /**
//  * Filter pro autorizaci uzivatelu (pomoci tokenu)
//  */
// public class TokenAuthorization extends OncePerRequestFilter {

// 	private final String PREFIX = "Bearer";

// 	private final String x_token;

// 	private final UserRepository repository;

// 	private final String[] ignoredEndpoints;

// 	/**
// 	 * Vytvori token filter
// 	 * 
// 	 * @param _x_token          Nazev fieldu v headeru requestu, ktery obsahuje
// 	 *                          pristupovy token
// 	 * @param _repository       Repozitar z uzivately
// 	 * @param _ignoredEndpoints Endpointy, ktery bude filter ignorovat
// 	 */
// 	public TokenAuthorization(String _x_token, UserRepository _repository, String[] _ignoredEndpoints) {
// 		this.x_token = _x_token;
// 		this.repository = _repository;
// 		this.ignoredEndpoints = _ignoredEndpoints;
// 	}

// 	@Override
// 	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
// 			throws ServletException, IOException {
// 		// endpoint filter
// 		if (this.ignoredEndpoints != null) {
// 			final String uri = request.getRequestURI();
// 			for (String ep : this.ignoredEndpoints) {
// 				if (ep.equals(uri)) {
// 					chain.doFilter(request, response);
// 					return;
// 				}
// 			}
// 		}

// 		// validace tokenu
// 		String msg;
// 		UserRC user = null;
// 		if ((user = validateToken(request)) != null) {
// 			if (setUpSpringAuthentication(user, request.getHeader(this.x_token))) {
// 				chain.doFilter(request, response);
// 				return;
// 			}
// 			msg = "You have no role";
// 		} else {
// 			msg = "Access token is invalid";
// 		}
// 		// pristup zamitnut
// 		SecurityContextHolder.clearContext();
// 		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
// 		ServletOutputStream outputStream = response.getOutputStream();
// 		outputStream.println(ResponseHandler.error(msg).toString());
// 		outputStream.flush();
// 	}

// 	/**
// 	 * Autentizace uzivatele
// 	 * 
// 	 * @param user Uzivatel ktery zada system o autentizaci
// 	 */
// 	private boolean setUpSpringAuthentication(UserRC user, String token) {
// 		// pokud uzivatel nema zadnou roli, nemuze pristoupit
// 		if (user.getRoles().isEmpty()) {
// 			return false;
// 		}

// 		// Set roly uzivatele prevede na kolekci SimpleGrantedAuthority
// 		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
// 		for (Role r : user.getRoles()) {
// 			authorities.add(new SimpleGrantedAuthority("ROLE_" + r.getName().toString()));
// 		}

// 		// Nastaveni spring security
// 		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, token,
// 				authorities);
// 		SecurityContextHolder.getContext().setAuthentication(auth);

// 		return true;
// 	}

// 	/**
// 	 * Validuje token a nejde v databazi uzivatele, kteremu nalezi a chce
// 	 * pristupovat k endpointu vyzadujicimu autorizaci (uzivatel musi byt prihlasen
// 	 * => jeho TOKEN je zapsan v databazi).
// 	 * Token se stava automaticky neplatnym po uplynuti
// 	 * definovaneho casu "GlobalConfig.TOKEN_VALIDITY_DURATION"
// 	 * 
// 	 * @param request HttpServletRequest
// 	 * @return UserRC
// 	 */
// 	// private UserRC validateToken(HttpServletRequest request) {
// 	// 	if (request == null) {
// 	// 		return null;
// 	// 	}

// 	// 	// pristopovy token
// 	// 	String accessToken = request.getHeader(this.x_token);

// 	// 	// token neni definovan
// 	// 	if (accessToken == null) {
// 	// 		return null;
// 	// 	}
// 	// 	if (accessToken.length() == 0) {
// 	// 		return null;
// 	// 	}

// 	// 	// prefix check
// 	// 	accessToken = accessToken.trim();
// 	// 	if (!accessToken.startsWith(PREFIX)) {
// 	// 		return null;
// 	// 	}
// 	// 	accessToken = accessToken.replace(PREFIX, "").trim();

// 	// 	// najde uzivatele podle pristupoveho tokenu
// 	// 	// Optional<UserRC> user = this.repository.findByToken(accessToken);
// 	// 	// if (!user.isPresent()) {
// 	// 	// 	return null;
// 	// 	// }
// 	// 	// overi casovou platnost
// 	// 	// Date now = new java.util.Date(Calendar.getInstance().getTime().getTime());

// 	// 	// if (user.get().getLastAccessTime() != null) {
// 	// 	// 	long diff = now.getTime() - user.get().getLastAccessTime().getTime();
// 	// 	// 	if (diff / (60 * 1000) > GlobalConfig.TOKEN_VALIDITY_DURATION) {
// 	// 	// 		user.get().setToken(null);
// 	// 	// 		this.repository.save(user.get());
// 	// 	// 		return null;
// 	// 	// 	}
// 	// 	// }

// 	// 	// refresh casu
// 	// 	// user.get().setLastAccessTime(now);
// 	// 	// this.repository.save(user.get());

// 	// 	// return user.get();
// 	// }

// 	/**
// 	 * Vygeneruje pristupovy token pro uzivatele
// 	 * 
// 	 * @param _user       Uzivatel, pro ktereho se ma vygenerovat token
// 	 * @param _repository Repozitar uzivatelu
// 	 * @return Pristupovy token
// 	 * @throws Exception
// 	 */
// 	// public static String generateAccessTokenForUser(UserRC _user, UserRepository _repository) throws Exception {
// 	// 	if (_user == null) {
// 	// 		throw new Exception("failure, user is null");
// 	// 	}
// 	// 	if (_repository == null) {
// 	// 		throw new Exception("failure, user repository is null");
// 	// 	}

// 	// 	// vygenerovani unikatniho pristupoveho tokenu
// 	// 	String token = "";
// 	// 	boolean success = false;
// 	// 	for (int i = 0; i < 1000; ++i) {
// 	// 		token = TokenAuthorization.generateToken();
// 	// 		if (!_repository.findByToken(token).isPresent()) {
// 	// 			success = true;
// 	// 			break;
// 	// 		}
// 	// 	}

// 	// 	// nepodarilo se vygenerovat pristupovy token
// 	// 	if (!success) {
// 	// 		throw new Exception("failed to generate access token");
// 	// 	}

// 	// 	// ulozi token a cas do databaze
// 	// 	_user.setToken(token);
// 	// 	_user.setLastAccessTime(new java.util.Date(Calendar.getInstance().getTime().getTime()));
// 	// 	_repository.save(_user);
// 	// 	return token;
// 	// }

// 	// private static final SecureRandom secureRandom = new SecureRandom();
// 	// private static final Encoder base64Encoder = Base64.getUrlEncoder();

// 	/**
// 	 * Nahodne vygeneruje token
// 	 * 
// 	 * @return Novy teoken
// 	 */
// 	// public static String generateToken() {
// 	// 	byte bytes[] = new byte[64];
// 	// 	secureRandom.nextBytes(bytes);
// 	// 	return base64Encoder.encodeToString(bytes);
// 	// }

// }